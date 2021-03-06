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
package com.od.jtimeseries.server.servermetrics;

import com.od.jtimeseries.component.managedmetric.AbstractManagedMetric;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.identifiable.Identifiable;
import com.od.jtimeseries.server.message.ServerSeriesUdpMessageListener;
import com.od.jtimeseries.source.Counter;
import com.od.jtimeseries.util.time.Time;
import com.od.jtimeseries.util.time.TimePeriod;
import static com.od.jtimeseries.capture.function.CaptureFunctions.LATEST;
import static com.od.jtimeseries.capture.function.CaptureFunctions.COUNT_OVER;
import static com.od.jtimeseries.capture.function.CaptureFunctions.MEAN_COUNT_OVER;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 25-Nov-2009
 * Time: 19:47:38
 * To change this template use File | Settings | File Templates.
 */
public class UpdatesReceivedMetric extends AbstractManagedMetric {

    private static final String id = "UdpSeriesUpdates";
    private TimePeriod countPeriod;
    private String parentContextPath;

    public UpdatesReceivedMetric(String parentContextPath) {
        this(parentContextPath, DEFAULT_TIME_PERIOD_FOR_SERVER_METRICS);
    }

    public UpdatesReceivedMetric(String parentContextPath, TimePeriod countPeriod) {
        this.parentContextPath = parentContextPath;
        this.countPeriod = countPeriod;
    }

    protected String getSeriesPath() {
        return parentContextPath + Identifiable.NAMESPACE_SEPARATOR + id;
    }

    public void doInitializeMetric(TimeSeriesContext rootContext, String path) {
        Counter counter = rootContext.createCounterSeries(
            path,
            "A count of series data updates via UDP messages",
            MEAN_COUNT_OVER(Time.seconds(1), countPeriod),
            LATEST(countPeriod)
        );
        ServerSeriesUdpMessageListener.setUpdateMessagesReceivedCounter(counter);
    }

}
