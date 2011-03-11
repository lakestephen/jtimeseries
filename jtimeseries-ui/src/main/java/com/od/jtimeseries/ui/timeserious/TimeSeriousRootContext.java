package com.od.jtimeseries.ui.timeserious;

import com.od.jtimeseries.context.ContextFactory;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.context.impl.DefaultContextFactory;
import com.od.jtimeseries.net.udp.TimeSeriesServer;
import com.od.jtimeseries.net.udp.TimeSeriesServerDictionary;
import com.od.jtimeseries.timeseries.TimeSeriesFactory;
import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
import com.od.jtimeseries.ui.download.panel.LoadSeriesFromServerCommand;
import com.od.jtimeseries.ui.download.panel.TimeSeriesServerContext;
import com.od.jtimeseries.ui.event.TimeSeriousBusListener;
import com.od.jtimeseries.ui.event.TimeSeriousBusListenerAdapter;
import com.od.jtimeseries.ui.timeseries.ServerTimeSeries;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
import com.od.jtimeseries.ui.timeseries.UiTimeSeriesConfig;
import com.od.jtimeseries.ui.timeserious.config.ConfigAware;
import com.od.jtimeseries.ui.timeserious.config.TimeSeriesServerConfig;
import com.od.jtimeseries.ui.timeserious.config.TimeSeriousConfig;
import com.od.jtimeseries.ui.visualizer.AbstractUIRootContext;
import com.od.jtimeseries.util.NamedExecutors;
import com.od.jtimeseries.util.identifiable.Identifiable;
import com.od.jtimeseries.util.identifiable.IdentifiableTreeEvent;
import com.od.jtimeseries.util.identifiable.IdentifiableTreeListenerAdapter;
import com.od.jtimeseries.util.logging.LogMethods;
import com.od.jtimeseries.util.logging.LogUtils;
import com.od.swing.eventbus.UIEventBus;
import com.od.swing.util.UIUtilities;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 13-Dec-2010
 * Time: 07:19:42
 * To change this template use File | Settings | File Templates.
 */
public class TimeSeriousRootContext extends AbstractUIRootContext implements ConfigAware {

    private static LogMethods logMethods = LogUtils.getLogMethods(TimeSeriousRootContext.class);
    private static final ScheduledExecutorService loadSeriesFromServerExecutor = NamedExecutors.newSingleThreadScheduledExecutor("LoadSeriesFromServer");
    private static final int SERVER_REFRESH_RATE_MINS = 20;

    private final ConcurrentMap<TimeSeriesServer, ScheduledFuture> loadTasksByServer = new ConcurrentHashMap<TimeSeriesServer, ScheduledFuture>();

    private DisplayNameCalculator displayNameCalculator;
    private VisualizerContext visualizerContext;


    public TimeSeriousRootContext(TimeSeriesServerDictionary serverDictionary, DisplayNameCalculator displayNameCalculator) {
        super(serverDictionary, displayNameCalculator);
        this.displayNameCalculator = displayNameCalculator;
        addTreeListener(new DisplayNameTreeListener());
        initializeFactoriesAndContextBusListener();

        //add a bus listener to create visualizer tree nodes locally, for the main tree selector
        UIEventBus.getInstance().addEventListener(
            TimeSeriousBusListener.class,
            new VisualizerTreeNodeBusListener()
        );
    }

    public void addVisualizerContext(VisualizerContext visualizerContext) {
        addChild(visualizerContext);
        this.visualizerContext = visualizerContext;
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
        config.setDisplayNamePatterns(displayNameCalculator.getDisplayNamePatterns());
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
        displayNameCalculator.setDisplayNamePatterns(config.getDisplayNamePatterns());
    }

    public List<ConfigAware> getConfigAwareChildren() {
        return Collections.emptyList();
    }

    public boolean containsVisualizerWithName(String name) {
        return visualizerContext.contains(name);
    }

    private class VisualizerTreeNodeBusListener extends TimeSeriousBusListenerAdapter {

        public void visualizerFrameDisplayed(VisualizerInternalFrame visualizerFrame) {
            VisualizerNode v = visualizerContext.get(visualizerFrame.getTitle(), VisualizerNode.class);
            if ( v == null ) {
                v = new VisualizerNode(visualizerFrame.getTitle(), visualizerFrame.getTitle(), visualizerFrame);
                v.shown(visualizerFrame);
                visualizerContext.addChild(v);
            } else {
                v.shown(visualizerFrame);
            }
        }

        public void visualizerFrameDisposed(VisualizerInternalFrame visualizerFrame) {
            VisualizerNode v = visualizerContext.get(visualizerFrame.getTitle(), VisualizerNode.class);
            if ( v != null) {
                v.hidden();
            }
        }

    }

    private class ServerSeriesLoadingBusListener extends ContextUpdatingBusListener {

        public ServerSeriesLoadingBusListener(TimeSeriesContext rootContext) {
            super(rootContext);
        }

        //add a time series server context when a new server is created, and load its series
        //visualizer contexts don't do this automatically otherwise the visualizers would end up with all the
        //known servers, even if they don't have any series
        public void serverAdded(TimeSeriesServer s) {
            TimeSeriesServerContext context = new TimeSeriesServerContext(TimeSeriousRootContext.this, s);
            TimeSeriousRootContext.this.addChild(context);
            addServerLoadTask(s);
        }

        public void serverRemoved(TimeSeriesServer s) {
            synchronized (loadTasksByServer) {
                ScheduledFuture f = loadTasksByServer.remove(s);
                f.cancel(false);
            }
            super.serverRemoved(s);
        }

        private void addServerLoadTask(final TimeSeriesServer s) {
            Runnable loadTask = new Runnable() {
                public void run() {
                    synchronized (loadTasksByServer) {
                        //if the server has been removed from the map, this means the user has removed the server
                        //we don't want to run the refresh
                        if ( loadTasksByServer.containsKey(s)) {
                            new LoadSeriesFromServerCommand(
                                TimeSeriousRootContext.this
                            ).execute(s);
                        }
                    }
                }
            };
            ScheduledFuture f = loadSeriesFromServerExecutor.scheduleWithFixedDelay(loadTask, 0, SERVER_REFRESH_RATE_MINS, TimeUnit.MINUTES);
            loadTasksByServer.put(s, f);
        }

    }

    private class RootContextTimeSeriesFactory extends AbstractUIContextTimeSeriesFactory {

        protected UIPropertiesTimeSeries createTimeSeriesForConfig(UiTimeSeriesConfig config) throws MalformedURLException {
            return new ServerTimeSeries(config);
        }
    }

    /**
     * Auto apply display name rules to series in the main selector
     */
    private class DisplayNameTreeListener extends IdentifiableTreeListenerAdapter {

        public void descendantAdded(IdentifiableTreeEvent contextTreeEvent) {
            for ( Identifiable i : contextTreeEvent.getNodes()) {
                final List<UIPropertiesTimeSeries> l = i.findAll(UIPropertiesTimeSeries.class).getAllMatches();
                UIUtilities.runInDispatchThread(new Runnable() {
                    public void run() {
                        for (UIPropertiesTimeSeries series : l) {
                            displayNameCalculator.setDisplayName(series);
                        }
                    }
                });

            }
        }
    }
}
