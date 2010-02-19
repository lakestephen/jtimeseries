/**
 * Copyright (C) 2009 (nick @ objectdefinitions.com)
 *
 * This file is part of JTimeseries.
 *
 * JTimeseries is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTimeseries is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.od.jtimeseries.context.impl;

import com.od.jtimeseries.capture.function.CaptureFunction;
import com.od.jtimeseries.capture.function.CaptureFunctions;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.source.*;
import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
import com.od.jtimeseries.util.time.TimePeriod;
import com.od.jtimeseries.util.identifiable.Identifiable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 05-Nov-2009
 * Time: 16:59:43
 *
 * The logic for creating all the sources, captures and series and binding them together if the user calls
 * ContextMetricCreator methods on the context. In general it is expected that there will not already by
 * sources and/or captures with the id specified, and if there are,  this will result in an AlreadyExistsException
 *
 * For TimeSeries, if an existing series is found, it will be assumed that the user wants to bind the source and capture
 * to the existing series. This is because there are many cases where persisted series are loaded by an application on startup,
 * but the app still needs to generate ValueRecorders and Captures etc which bind to those series, so that new values can be
 * recorded. So it is a big help in this case if we can simply bind the recorders/captures to the reloaded series, without
 * having to have special logic to check if they already exist and adjust behaviour accordingly.
 */
class DefaultMetricCreator implements ContextMetricCreator {

    private TimeSeriesContext timeSeriesContext;
    protected static AtomicLong captureUniqueId = new AtomicLong(0);

    //the user passes in just one id, which will be used for the timeseries, but the value source and
    //capture also require ids. We create these by adding a prefix to the timeseries id supplied.
    private static String SOURCE_PREFIX = "Source_";
    private static String CAPTURE_PREFIX = "Capture_";

    public DefaultMetricCreator(TimeSeriesContext timeSeriesContext) {
        this.timeSeriesContext = timeSeriesContext;
    }

    public ValueRecorder createValueRecorderSeries(Identifiable parent, String path, String id, String description, CaptureFunction... captureFunctions) {
        ValueRecorder v = timeSeriesContext.createValueRecorder(SOURCE_PREFIX + id, description);
        createSeriesAndCapturesForSource(id, description, v, captureFunctions);
        return v;
    }

    public QueueTimer createQueueTimerSeries(Identifiable parent, String path, String id, String description, CaptureFunction... captureFunctions) {
        QueueTimer q = timeSeriesContext.createQueueTimer(SOURCE_PREFIX + id, description);
        createSeriesAndCapturesForSource(id, description, q, captureFunctions);
        return q;
    }

    public Counter createCounterSeries(Identifiable parent, String path, String id, String description, CaptureFunction... captureFunctions) {
        Counter c = timeSeriesContext.createCounter(SOURCE_PREFIX + id, description);
        createSeriesAndCapturesForSource(id, description, c, captureFunctions);
        return c;
    }

    public EventTimer createEventTimerSeries(Identifiable parent, String path, String id, String description, CaptureFunction... captureFunctions) {
        EventTimer m = timeSeriesContext.createEventTimer(SOURCE_PREFIX + id, description);
        createSeriesAndCapturesForSource(id, description, m, captureFunctions);
        return m;
    }

    public TimedValueSource createValueSupplierSeries(Identifiable parent, String path, String id, String description, ValueSupplier valueSupplier, TimePeriod timePeriod) {
        TimedValueSource s = timeSeriesContext.createTimedValueSource(SOURCE_PREFIX + id, description, valueSupplier, timePeriod);
        createSeriesAndCapturesForSource(id, description, s);
        return s;
    }

    private void createSeriesAndCapturesForSource(String id, String description, ValueSource source, CaptureFunction... captureFunctions) {
        //if no functions are specified, assume that means we want to capture the raw values to a time series
        if (captureFunctions.length == 0) {
            createRawValueSeriesAndCapture(id, description, source);
        } else {
            for (CaptureFunction captureFunction : captureFunctions) {
                //handle the special 'RAW Values' functon which can be added to the list to mean
                //'in addition to these time based function, we also want to capture the raw values'
                if ( captureFunction == CaptureFunctions.RAW_VALUES) {
                    createRawValueSeriesAndCapture(id, description, source);
                } else {
                    createCaptureFunctionSeriesAndCapture(id, description, source, captureFunction);
                }
            }
        }
    }

    private void createCaptureFunctionSeriesAndCapture(String id, String description, ValueSource source, CaptureFunction captureFunction) {
        IdentifiableTimeSeries series = createTimeSeriesIfNotPresent(id, description, captureFunction);
        String captureId = getNextCaptureId(id);
        timeSeriesContext.createTimedCapture(captureId, source, series, captureFunction);
    }

    private void createRawValueSeriesAndCapture(String id, String description, ValueSource source) {
        IdentifiableTimeSeries series = createTimeSeriesIfNotPresent(id, description, null);
        String captureId = getNextCaptureId(id);
        timeSeriesContext.createCapture(captureId, source, series);
    }

    private IdentifiableTimeSeries createTimeSeriesIfNotPresent(String id, String description, CaptureFunction captureFunction) {
        //if there is a function we use this to create the id and description for the timeseries
        //otherwise for 'raw' values series, the id becomes the id of the timeseries literally
        if ( captureFunction != null) {
            id = id + " " + captureFunction.getDescription();
        }

        IdentifiableTimeSeries series = timeSeriesContext.getTimeSeries(id);
        if ( series == null) {
            series = timeSeriesContext.createTimeSeries(id, description);
        }
        return series;
    }

    private String getNextCaptureId(String id) {
        return CAPTURE_PREFIX + id + "_" + captureUniqueId.incrementAndGet();
    }

}
