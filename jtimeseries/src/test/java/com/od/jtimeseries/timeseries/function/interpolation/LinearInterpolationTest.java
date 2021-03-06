package com.od.jtimeseries.timeseries.function.interpolation;

import com.od.jtimeseries.timeseries.Item;
import com.od.jtimeseries.timeseries.IndexedTimeSeries;
import com.od.jtimeseries.timeseries.TimeSeriesItem;
import com.od.jtimeseries.timeseries.impl.DefaultTimeSeries;
import com.od.jtimeseries.util.numeric.LongNumeric;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 08-Mar-2009
 * Time: 01:01:15
 * To change this template use File | Settings | File Templates.
 */
public class LinearInterpolationTest extends Assert {

    @Test
    public void testLinearInterpolation() {
        IndexedTimeSeries t = new DefaultTimeSeries();
        t.addItem(new Item(1000, 1000));
        t.addItem(new Item(2000, 2000));

        LinearInterpolationFunction l = new LinearInterpolationFunction();

        TimeSeriesItem i = l.calculateInterpolatedValue(t, 1000, t.getItem(0), t.getItem(0));
        assertEquals(1000, i.getValue().longValue());

        i = l.calculateInterpolatedValue(t, 1500, t.getItem(0), t.getItem(1));
        assertEquals(1500, i.getValue().longValue());

        i = l.calculateInterpolatedValue(t, 1750, t.getItem(0), t.getItem(1));
        assertEquals(1750, i.getValue().longValue());

        t.addItem(new Item(3000, 3));
        t.addItem(new Item(4000, 4));
        i = l.calculateInterpolatedValue(t, 3500, t.getItem(2), t.getItem(3));
        assertEquals(3.5, i.getValue().doubleValue());
    }
}
