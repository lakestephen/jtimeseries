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
package com.od.jtimeseries.ui.visualizer.chart;

import com.od.jtimeseries.ui.config.ChartRangeMode;
import com.od.jtimeseries.ui.config.DomainTimeSelection;
import com.od.jtimeseries.ui.timeseries.ChartingTimeSeries;
import com.od.jtimeseries.ui.visualizer.chart.creator.*;
import com.od.jtimeseries.util.NamedExecutors;
import com.od.swing.progress.ProgressLayeredPane;
import com.od.swing.progress.RotatingImageSource;
import com.od.swing.util.AwtSafeListener;
import com.od.swing.weakreferencelistener.WeakReferenceListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.ChartChangeListener;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 07-Jan-2009
 * Time: 14:28:23
 */
public class TimeSeriesChart extends JPanel {

    private static ScheduledExecutorService stopProgressExecutor = NamedExecutors.newSingleThreadScheduledExecutor("TimeSeriesChart-ScheduledExecutor");
    private static final int MIN_ANIMATION_TIME_MILLIS = 500; //showing for less time than this can just flash the animation on, which looks bad

    private String title;
    private List<ChartingTimeSeries> timeSeriesList = Collections.EMPTY_LIST;
    private ChartPanel chartPanel;
    private JPanel noChartsPanel = new JPanel();
    private ChartRangeMode chartRangeMode = ChartRangeMode.RangePerId;
    private Color chartBackgroundColor = Color.WHITE;
    private ChartType chartType = ChartType.DEFAULT_CHART_TYPE;
    private ChartDataFilter chartDataFilter = ChartDataFilter.NoFilter;
    private ProgressLayeredPane progressPane = new ProgressLayeredPane(noChartsPanel, 0.4f, 20, 18);
    private JFreeChart chart;
    private boolean showLegend = true;
    private DomainTimeSelection domainStartTimeSelection = new DomainTimeSelection();
    private final ChartCreatorFactory chartCreatorFactory = new ChartCreatorFactory();
    private long animationLastStartedTimestamp;

    private PropertyChangeListener loadingProgressPropertyChangeListener = AwtSafeListener.getAwtSafeListener(
            new LoadingProgressPropertyChangeListener(), PropertyChangeListener.class);

    private PropertyChangeListener refreshChartPropertyListener = AwtSafeListener.getAwtSafeListener(
            new RefreshChartPropertyChangeListener(), PropertyChangeListener.class);

    private ChartTitleListener chartTitleListener = new ChartTitleListener();

    private List<WeakReferenceListener> weakReferencePropertyListeners = new LinkedList<WeakReferenceListener>();

    public TimeSeriesChart(String title) {
        this.title = title;
        setLayout(new BorderLayout());
        createNoChartsPanel();
        progressPane.setIconSource(new RotatingImageSource(
            "/progressAnimation/progress.png", 16, 80, 80, 0.5f
        ));
        progressPane.setDelayBetweenFrames(200);
        add(progressPane, BorderLayout.CENTER);
        createWeakReferenceListeners();
    }

    private void createWeakReferenceListeners() {
        //add week reference listeners to the charting time series, so that references back from the model don't retain this timeSeriesChart
        weakReferencePropertyListeners.add(new WeakReferenceListener(ChartingTimeSeries.DISPLAY_NAME_PROPERTY, refreshChartPropertyListener));
        weakReferencePropertyListeners.add(new WeakReferenceListener(ChartingTimeSeries.COLOUR_PROPERTY, refreshChartPropertyListener));
        weakReferencePropertyListeners.add(new WeakReferenceListener(ChartingTimeSeries.LOADED_PROPERTY, loadingProgressPropertyChangeListener));
        weakReferencePropertyListeners.add(new WeakReferenceListener(ChartingTimeSeries.STALE_PROPERTY, loadingProgressPropertyChangeListener));
    }

    private void createNoChartsPanel() {
        noChartsPanel.setLayout(new BorderLayout());
        JLabel noSeriesLabel = new JLabel("No Timeseries Selected");
        noSeriesLabel.setHorizontalAlignment(JLabel.CENTER);
        noSeriesLabel.setVerticalAlignment(JLabel.CENTER);
        noChartsPanel.add(noSeriesLabel);
    }

    public void setSeries(List<ChartingTimeSeries> newSelection) {
        removePropertyListener(this.timeSeriesList);

        //our own copy guaranteed to be RandomAccess list
        newSelection = new ArrayList<ChartingTimeSeries>(newSelection);
        addPropertyListener(newSelection);
        this.timeSeriesList = newSelection;
        if ( timeSeriesList.size() == 0) {
            if ( chartPanel != null) {
                chartPanel = null;
                chart = null;
            }
            progressPane.setViewComponent(noChartsPanel);
        } else {
            createAndSetChart();
            progressPane.setViewComponent(chartPanel);
        }
        validate();
        repaint();
        showOrStopProgressAnimation();
    }

    //check whether the progress animation should be shown, and if so, show it, if not stop it
    private void showOrStopProgressAnimation() {
        boolean showProgress = false;
        for ( ChartingTimeSeries s : timeSeriesList) {
            //if not loaded and not stale, should be queued to load
            if ( ! s.isLoaded() && ! s.isStale()) {
                showProgress = true;
                break;
            }
        }

        if ( showProgress ) {
            animationLastStartedTimestamp = System.currentTimeMillis();
            progressPane.startProgressAnimation("Loading Series Data");
        } else {
            long minAnimationTimeRemaining = MIN_ANIMATION_TIME_MILLIS - (System.currentTimeMillis() - animationLastStartedTimestamp);
            if ( minAnimationTimeRemaining < 0 ) {
                progressPane.stopProgressAnimation();
            } else {
                scheduleStopProgress(minAnimationTimeRemaining);
            }
        }

    }

    private void scheduleStopProgress(long minTimeRemaining) {
        stopProgressExecutor.schedule(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showOrStopProgressAnimation();
                    }
                });
            }
        }, minTimeRemaining, TimeUnit.MILLISECONDS);
    }

    public void setChartBackgroundColor(Color c) {
        if ( chartBackgroundColor != c) {
            chartBackgroundColor = c;
            createAndSetChart();
        }
    }

    public Color getChartBackgroundColor() {
        return chartBackgroundColor;
    }

    private void addPropertyListener(List<ChartingTimeSeries> newSelection) {
        for ( ChartingTimeSeries s : newSelection) {
             for ( WeakReferenceListener l : weakReferencePropertyListeners) {
                l.addListenerTo(s);
             }
        }
    }

    private void removePropertyListener(List<ChartingTimeSeries> timeSeriesList) {
         for ( ChartingTimeSeries s : timeSeriesList) {
             for ( WeakReferenceListener l : weakReferencePropertyListeners) {
                l.removeListenerFrom(s);
             }
        }
    }

    public ChartRangeMode getChartRangeMode() {
        return chartRangeMode;
    }

    public void setChartRangeMode(ChartRangeMode m ) {
        if ( this.chartRangeMode != m ) {
            this.chartRangeMode = m;
            createAndSetChart();
        }
    }

    public DomainTimeSelection getDomainStartTimeSelection() {
        return domainStartTimeSelection;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        if ( this.chartType != chartType) {
            this.chartType = chartType;
            createAndSetChart();
        }
    }

    public ChartDataFilter getChartDataFilter() {
        return chartDataFilter;
    }


    public void setChartDataFilter(ChartDataFilter f) {
        if ( this.chartDataFilter != f) {
            this.chartDataFilter = f;
            createAndSetChart();
        }
    }

    public void setDomainStartTimeSelection(DomainTimeSelection newValue) {
        if ( ! this.domainStartTimeSelection.equals(newValue) ) {
            this.domainStartTimeSelection = newValue;
            createAndSetChart();
        }
    }

    public void setShowLegend(boolean showLegend) {
        if ( this.showLegend != showLegend) {
            this.showLegend = showLegend;
            createAndSetChart();
        }
    }

    public boolean isShowLegend() {
        return this.showLegend;
    }

    private void createAndSetChart() {
        ChartCreatorParameters p = new ChartCreatorParameters(chartRangeMode, domainStartTimeSelection, chartBackgroundColor, timeSeriesList, showLegend, title, chartDataFilter);
        AbstractXYChartCreator chartCreator = chartCreatorFactory.getChartCreator(chartType, p);
        chart = chartCreator.createNewChart();
        addTitleChangeListener(chart);
        if ( chartPanel == null ) {
            chartPanel = new ChartPanel(chart);
        } else {
            chartPanel.setChart(chart);
        }
    }

    private void addTitleChangeListener(final JFreeChart chart) {
        WeakReferenceListener l = new WeakReferenceListener(chartTitleListener);
        l.addListenerTo(chart);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title =  title;
        if ( chart != null ) {
            chart.setTitle(title);
        }
    }

    private class LoadingProgressPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            showOrStopProgressAnimation();
        }
    }

    private class RefreshChartPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            createAndSetChart();
        }
    }

    private class ChartTitleListener implements ChartChangeListener {

        public void chartChanged(ChartChangeEvent event) {
            if ( event.getType() == ChartChangeEventType.GENERAL) {
                TimeSeriesChart.this.title = chart.getTitle().getText();
            }
        }
    }
}
