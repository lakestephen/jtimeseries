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
package com.od.jtimeseries.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 24-Feb-2009
 * Time: 17:53:04
 */
public class TimeSeriesExecutorFactory {

    private static volatile ExecutorSource executorSource = new ExecutorSource() {

        private ExecutorService timeSeriesEventExecutor = NamedExecutors.newSingleThreadExecutor("TimeSeriesEvent");
        private ExecutorService captureEventExecutor = NamedExecutors.newSingleThreadExecutor("CaptureEvent");
        private ExecutorService identifiableEventExecutor = NamedExecutors.newSingleThreadExecutor("IdentifiableEvent");        

        public Executor getExecutorForTimeSeriesEvents(Object timeSeries) {
            return timeSeriesEventExecutor;
        }

        public Executor getExecutorForCaptureEvents(Object capture) {
            return captureEventExecutor;
        }

        public Executor getExecutorForIdentifiableTreeEvents(Object identifiable) {
            return identifiableEventExecutor;
        }
    };

    public static Executor getExecutorForTimeSeriesEvents(Object timeSeries) {
        return executorSource.getExecutorForTimeSeriesEvents(timeSeries);
    }

    public static Executor getExecutorForCaptureEvents(Object capture) {
        return executorSource.getExecutorForCaptureEvents(capture);
    }

    public static Executor getExecutorForIdentifiableTreeEvents(Object identifiable) {
        return executorSource.getExecutorForIdentifiableTreeEvents(identifiable);
    }

    public static ExecutorSource getExecutorSource() {
        return executorSource;
    }

    public static void setExecutorSource(ExecutorSource executorSource) {
        TimeSeriesExecutorFactory.executorSource = executorSource;
    }

    public static interface ExecutorSource {

        /**
         * Should return a single threaded executor if the guaranteed ordering of events
         * is to be preserved
         */
        Executor getExecutorForTimeSeriesEvents(Object timeSeries);

        /**
         * Should return a single threaded executor if the guaranteed ordering of events
         * is to be preserved
         */
        Executor getExecutorForCaptureEvents(Object capture);

        /**
         * Should return a single threaded executor if the guaranteed ordering of events
         * is to be preserved
         */
        Executor getExecutorForIdentifiableTreeEvents(Object identifiable);
    }
}
