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
package com.od.jtimeseries.capture.function;

import com.od.jtimeseries.timeseries.function.aggregate.AggregateFunction;
import com.od.jtimeseries.util.time.TimePeriod;

/**
 * Supplies Aggregate functions which can be used to aggregate the values recorded
 * during a given TimePeriod
 */
public interface CaptureFunction {

    /**
     * @return the AggregateFunction instance used as the prototype for creating subsequent function instances
     */
    AggregateFunction getPrototypeFunction();

    /**
     * @return a new function instance to process values from a source
     * @param oldFunctionInstance
     */
    AggregateFunction nextFunctionInstance(AggregateFunction oldFunctionInstance);


    String getDescription();


    TimePeriod getCapturePeriod();

}
