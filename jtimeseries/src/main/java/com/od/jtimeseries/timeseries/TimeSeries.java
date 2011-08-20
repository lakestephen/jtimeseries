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
package com.od.jtimeseries.timeseries;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 14-Nov-2009
 * Time: 16:15:57
 *
 * A TimeSeries is an ordered sequence of TimeSeriesItems, each representing a value at a point in time
 * The timepoints may or may not be equally spaced / periodic
 *
 * This interface defines a very limited set of common methods, but implementations may be based on varying datastructures,
 * and the choice of implementation may depend on the intended use. For example, an implementation based
 * on an array may provide rapid random access to the items by index (but may have poor performance for inserting
 * or merging TimeSeriesItems). Alternatively a TimeSeries based on a map or tree may offer fast search for an
 * item with a given timestamp and fast merging, but poor random access.
 *
 * Depending on the implementation, a TimeSeries may or may not allow more than one item with the same timestamp.
 * (Due to the lack of granularity in a system clock, an attempt to add items with a duplicate timestamp may be
 * likely, and it is probably best to let an application/implementation decide how to handle this case).
 *
 * In general, to allow eash comparisons, TimeSeries should implement equals and hashCode using the same contract which would be used in a
 * List implementation, e.g. the same as one would expect for List<TimeSeriesItem>.equals() and List<TimeSeriesItem>.hashCode()
 *
 * Note on thread safety:
 * 
 * TimeSeries implementations are in general intended to be thread safe (if not, this should be documented).
 * Furthermore, the guard used to guarantee thread safety should be the monitor/mutex of the time series instance
 * itself rather than that of an encapsulated private lock object. This is to enable client classes to safely
 * implement operations involving more than one method call by holding the lock on a series instance while the
 * operation takes place. In cases where the a timeseries implementation wraps another time series instance, client
 * classes in general should not change data of the wrapped instance directly - this should be done via the wrapper,
 * since making direct changes in this case would violate the guard provided by the wrapper instance's mutex/lock.
 */
public interface TimeSeries extends Iterable<TimeSeriesItem> {

    /**
     * @return the item in the series with the earliest timestamp value, or null if no items exist.
     */
    TimeSeriesItem getEarliestItem();

    /**
     * @return the item in the series with the latest timestamp value, or null if no items exist.
     */
    TimeSeriesItem getLatestItem();

    /**
     * @return timestamp of earliest item, or -1 if series is empty
     */
    long getEarliestTimestamp();

    /**
     * @return timestamp of latest item, or -1 if series is empty
     */
    long getLatestTimestamp();

    /**
     * Add an item to the series. In series which support multiple items per timestamp, this item should
     * appear after any existing items which share its timestamp
     */
    void addItem(TimeSeriesItem timeSeriesItem);


    /**
     * @return true, if item was removed, false if item was not in the timeseries
     */
    boolean removeItem(TimeSeriesItem timeSeriesItem);


    int size();

    /**
     * note, time series events are fired asynchronously on an event notification thread
     * @param l add a time series listener to receive change events from the time series
     */
    void addTimeSeriesListener(TimeSeriesListener l);

    /**
     * @param l remove a time series listener
     */
    void removeTimeSeriesListener(TimeSeriesListener l);

    /**
     * modCount should increase by at least 1 whenever series data is modified
     * It can be used to detect that changes have occurred
     *
     * TimeSeriesEvent also supply the new modCount of the series
     *
     * @return the modification count of this timeseries
     */
    long getModCount();


    List<TimeSeriesItem> getSnapshot();


    void clear();
}
