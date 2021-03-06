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
package com.od.jtimeseries.server.summarystats;

import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
import com.od.jtimeseries.timeseries.TimeSeries;
import com.od.jtimeseries.util.numeric.Numeric;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 21-Feb-2010
 * Time: 10:34:18
 * To change this template use File | Settings | File Templates.
 */
public interface SummaryStatistic {


    public String getStatisticName();

    /**
     * @return true, if a stat should be recalculated for series with these times properties
     */
    boolean shouldRecalc(long latestTimestampInSeries, long lastRecalcTimestamp);

    /**
     * Recacalculate the summary stat
     */
    void recalcSummaryStatistic(IdentifiableTimeSeries timeSeries);

    /**
     * @return true, if a stat should be recalculated for series with these times properties
     */
    boolean shouldDelete(long latestTimestampInSeries, long lastRecalcTimestamp);

    /**
     * Delete the summary stat
     */
    void deleteSummaryStatistic(IdentifiableTimeSeries series);

    String getSummaryStatProperty();
}
