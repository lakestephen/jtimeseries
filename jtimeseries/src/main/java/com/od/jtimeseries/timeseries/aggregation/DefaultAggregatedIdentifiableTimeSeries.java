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
package com.od.jtimeseries.timeseries.aggregation;

import com.od.jtimeseries.timeseries.TimeSeries;
import com.od.jtimeseries.timeseries.function.aggregate.AggregateFunction;
import com.od.jtimeseries.timeseries.impl.DefaultIdentifiableTimeSeries;
import com.od.jtimeseries.timeseries.impl.DefaultTimeSeries;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 21-Jan-2009
 * Time: 16:31:08
 */
public class DefaultAggregatedIdentifiableTimeSeries extends DefaultIdentifiableTimeSeries implements AggregatedIdentifiableTimeSeries {

    private DefaultAggregatedTimeSeries series;

    public DefaultAggregatedIdentifiableTimeSeries(String id, String description, AggregateFunction aggregateFunction) {
        super(id, description, new DefaultTimeSeries());
        this.series = new DefaultAggregatedTimeSeries(this, aggregateFunction);
    }

    public synchronized void addTimeSeries(TimeSeries... timeSeries) {
        series.addTimeSeries(timeSeries);
    }

}
