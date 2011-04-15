package com.od.jtimeseries.ui.timeserious;

import com.od.jtimeseries.context.ContextFactory;
import com.od.jtimeseries.context.impl.DefaultContextFactory;
import com.od.jtimeseries.net.udp.TimeSeriesServerDictionary;
import com.od.jtimeseries.timeseries.TimeSeriesFactory;
import com.od.jtimeseries.ui.config.*;
import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
import com.od.jtimeseries.ui.download.panel.TimeSeriesServerContext;
import com.od.jtimeseries.ui.timeseries.ServerTimeSeries;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
import com.od.jtimeseries.ui.visualizer.AbstractUIRootContext;
import com.od.jtimeseries.util.logging.LogMethods;
import com.od.jtimeseries.util.logging.LogUtils;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 13-Dec-2010
 * Time: 07:19:42
 * To change this template use File | Settings | File Templates.
 */
public class TimeSeriousRootContext extends AbstractUIRootContext implements ConfigAware {

    private static LogMethods logMethods = LogUtils.getLogMethods(TimeSeriousRootContext.class);

    private DesktopContext mainDesktopContext = new DesktopContext(DesktopConfiguration.MAIN_DESKTOP_NAME);

    public TimeSeriousRootContext(TimeSeriesServerDictionary serverDictionary, DisplayNameCalculator displayNameCalculator) {
        super(serverDictionary, displayNameCalculator);
        addTreeListener(new DisplayNameCalculatingTreeListener(displayNameCalculator));
        initializeFactoriesAndContextBusListener();
        addChild(mainDesktopContext);
    }

    public DesktopContext getMainDesktopContext() {
        return mainDesktopContext;
    }

    protected ContextFactory createContextFactory() {
        return new DefaultContextFactory();
    }

    protected TimeSeriesFactory createTimeSeriesFactory() {
        return new RootContextTimeSeriesFactory();
    }

    protected ContextUpdatingBusListener createContextBusListener() {
        return new ServerSeriesLoadingBusListener(this);
    }

    public void prepareConfigForSave(TimeSeriousConfig config) {
        List<TimeSeriesServerContext> serverContexts = findAll(TimeSeriesServerContext.class).getAllMatches();

        List<TimeSeriesServerConfig> serverConfigs = new LinkedList<TimeSeriesServerConfig>();
        for ( TimeSeriesServerContext c : serverContexts) {
            serverConfigs.add(new TimeSeriesServerConfig(c));
        }
        config.setTimeSeriesServerConfigs(serverConfigs);

        for ( DesktopContext desktopContext : findAll(DesktopContext.class).getAllMatches()) {
            config.setDesktopConfigration(desktopContext.getId(), desktopContext.getDesktopConfiguration());
        }
    }

    public void restoreConfig(TimeSeriousConfig config) {
        for (TimeSeriesServerConfig c : config.getServerConfigs()) {
            try {
                serverDictionary.getOrCreateServer(
                    c.getHostName(),
                    c.getPort(),
                    c.getDescription()
                );
            } catch (UnknownHostException e) {
                logMethods.logError("Could not create server " + serverDictionary, e);
            }
        }

        for ( Map.Entry<String, DesktopConfiguration> configEntry : config.getDesktopConfigs().entrySet()) {
            DesktopContext desktopContext = getOrCreateDesktopContext(configEntry.getValue());
            desktopContext.createDesktopConfiguration(configEntry.getValue());
        }
    }

    private DesktopContext getOrCreateDesktopContext(DesktopConfiguration desktopConfiguration) {
        DesktopContext context = (DesktopContext)get(desktopConfiguration.getDesktopName());
        if ( context == null) {
            context = new DesktopContext(desktopConfiguration.getDesktopName());
            addChild(context);
        }
        return context;
    }

    public List<ConfigAware> getConfigAwareChildren() {
        return Collections.emptyList();
    }

    //create ServerTimeSeries, which are lighter weight and not backed by an HttpTimeSeries
    //we don't want to create a RemoteHttpTimeSeries for every series in the main selector tree
    private class RootContextTimeSeriesFactory extends AbstractUIContextTimeSeriesFactory {

        protected UIPropertiesTimeSeries createTimeSeriesForConfig(UiTimeSeriesConfig config) throws MalformedURLException {
            return new ServerTimeSeries(config);
        }
    }

}
