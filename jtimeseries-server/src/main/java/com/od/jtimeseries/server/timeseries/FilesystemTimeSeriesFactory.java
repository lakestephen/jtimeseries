/**
 * Copyright (C) 2011 (nick @ objectdefinitions.com)
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
package com.od.jtimeseries.server.timeseries;

import com.od.jtimeseries.component.util.cache.TimeSeriesCache;
import com.od.jtimeseries.identifiable.Identifiable;
import com.od.jtimeseries.server.serialization.FileHeader;
import com.od.jtimeseries.server.serialization.SerializationException;
import com.od.jtimeseries.server.serialization.TimeSeriesSerializer;
import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
import com.od.jtimeseries.timeseries.impl.DefaultTimeSeriesFactory;
import com.od.jtimeseries.timeseries.impl.RoundRobinTimeSeries;
import com.od.jtimeseries.util.time.TimePeriod;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 20-May-2009
 * Time: 22:08:50
 * To change this template use File | Settings | File Templates.
 */
public class FilesystemTimeSeriesFactory extends DefaultTimeSeriesFactory {

    private TimeSeriesSerializer timeseriesSerializer;
    private TimePeriod fileAppendDelay;
    private TimePeriod fileRewriteDelay;
    private int seriesLength;
    private TimeSeriesCache<Identifiable,RoundRobinTimeSeries> timeSeriesCache;

    public FilesystemTimeSeriesFactory(TimeSeriesSerializer timeseriesSerializer, TimeSeriesCache<Identifiable,RoundRobinTimeSeries> timeSeriesCache, TimePeriod fileAppendDelay, TimePeriod fileRewriteDelay, int seriesLength) {
        this.timeseriesSerializer = timeseriesSerializer;
        this.timeSeriesCache = timeSeriesCache;
        this.fileAppendDelay = fileAppendDelay;
        this.fileRewriteDelay = fileRewriteDelay;
        this.seriesLength = seriesLength;
    }

    public IdentifiableTimeSeries createTimeSeries(Identifiable parent, String path, String id, String description, Class classType, Object... parameters) {
        if ( classType.isAssignableFrom(FilesystemTimeSeries.class)) {
            try {
                IdentifiableTimeSeries result;
                if ( parameters.length == 1 && parameters[0] instanceof FileHeader) {
                    FileHeader h = (FileHeader)parameters[0];
                    //series exists on disk already, we have a header
                    result = new FilesystemTimeSeries(h, timeseriesSerializer, timeSeriesCache, fileAppendDelay, fileRewriteDelay);
                } else {
                    result = new FilesystemTimeSeries(parent.getPath(), id, description, timeseriesSerializer, timeSeriesCache, seriesLength, fileAppendDelay, fileRewriteDelay);
                }
                return result;
            } catch (SerializationException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to create timeseries", e);
            }
        } else {
            return super.createTimeSeries(parent, path, id, description, classType, parameters);
        }
    }
}
