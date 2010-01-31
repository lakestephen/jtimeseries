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
package com.od.jtimeseries.timeseries.function.aggregate;

import com.od.jtimeseries.util.numeric.Numeric;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 01-Feb-2009
 * Time: 22:42:17
 * To change this template use File | Settings | File Templates.
 */
public class AggregateFunctions {

    public static AggregateFunction MAX() {
        return new MaxFunction();
    }

    public static AggregateFunction MEAN() {
        return new MeanFunction();
    }

    public static AggregateFunction MIN() {
        return new MinFunction();
    }

    public static AggregateFunction SUM() {
        return new SumFunction();
    }

    public static AggregateFunction COUNT() {
        return new CountFunction();
    }

    public static AggregateFunction CHANGE() {
        return new ChangeFunction();
    }

    public static AggregateFunction CHANGE(Numeric initialValue) {
        return new ChangeFunction(initialValue);
    }

    public static AggregateFunction CHANGE(String functionName, Numeric initialValue) {
        return new ChangeFunction(functionName, initialValue);
    }
}
