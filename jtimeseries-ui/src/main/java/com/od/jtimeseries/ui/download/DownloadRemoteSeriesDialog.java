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
package com.od.jtimeseries.ui.download;

import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.identifiable.Identifiable;
import com.od.jtimeseries.net.udp.TimeSeriesServerDictionary;
import com.od.jtimeseries.ui.config.UiTimeSeriesConfig;
import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
import com.od.jtimeseries.ui.download.panel.AbstractDownloadWizardPanel;
import com.od.jtimeseries.ui.download.panel.ChooseSeriesPanel;
import com.od.jtimeseries.ui.download.panel.SelectServerPanel;
import com.od.jtimeseries.ui.selector.shared.AbstractUIContextTimeSeriesFactory;
import com.od.jtimeseries.ui.selector.shared.ServerContextCreatingContextFactory;
import com.od.jtimeseries.ui.timeseries.ServerTimeSeries;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
import com.od.jtimeseries.ui.uicontext.*;
import com.od.swing.progress.ProgressLayeredPane;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 24-May-2009
 * Time: 22:30:18
 * To change this template use File | Settings | File Templates.
 */
public class DownloadRemoteSeriesDialog extends JFrame {

    private ProgressLayeredPane progressLayeredPane;
    private AbstractDownloadWizardPanel.WizardPanelListener panelListener;
    private SelectServerPanel selectServerPanel;
    private AbstractUIRootContext contextToStoreRemoteSeries;
    private ChooseSeriesPanel chooseSeriesPanel;
    private TimeSeriesContext destinationRootContext;

    public DownloadRemoteSeriesDialog(TimeSeriesServerDictionary serverDictionary, JComponent dialogPositionComponent, TimeSeriesContext destinationRootContext, DisplayNameCalculator displayNameCalculator) {
        //context into which we want to add selected series from remote server
        this.destinationRootContext = destinationRootContext;

        //a temporary context to use for times series selector, so that we can display all remote series
        //and give the user a chance to select the ones to add to destinationContext
        contextToStoreRemoteSeries = new SelectionRootContext(serverDictionary, displayNameCalculator);

        setTitle("Download Time Series");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        createPanelListener();
        createPanels(serverDictionary);

        progressLayeredPane = new ProgressLayeredPane(selectServerPanel);
        getContentPane().add(progressLayeredPane);

        setSize(AbstractDownloadWizardPanel.PANEL_WIDTH, AbstractDownloadWizardPanel.PANEL_HEIGHT);
        setLocationRelativeTo(dialogPositionComponent);
    }

    private void createPanels(TimeSeriesServerDictionary serverDictionary) {
        selectServerPanel = new SelectServerPanel(panelListener, serverDictionary, contextToStoreRemoteSeries);
    }

    private void createPanelListener() {
        panelListener = new AbstractDownloadWizardPanel.WizardPanelListener() {

            public void seriesLoaded() {
                chooseSeriesPanel = new ChooseSeriesPanel(panelListener, contextToStoreRemoteSeries);
                progressLayeredPane.setViewComponent(
                    chooseSeriesPanel
                );
            }

            public void seriesSelected(java.util.List<? extends UIPropertiesTimeSeries> series) {
                addSelectedSeriesToDestinationContext(series);
                dispose();
            }

            public void downloadCancelled() {
                dispose();
            }
        };
    }

    private void addSelectedSeriesToDestinationContext(List<? extends UIPropertiesTimeSeries> series) {
        for ( UIPropertiesTimeSeries p : series ) {
            String path = p.getPath();
            UiTimeSeriesConfig config = p.getConfig();
            //TODO - should we add extra handling if series already exists in target?
            if (!destinationRootContext.contains(path)) {
                //we don't know what type of UIPropertiesTimeSeries the destination context should contain
                //defer construction to the context's factories by using the generic create method on context
                destinationRootContext.create(
                        path,
                        p.getDescription(),
                        UIPropertiesTimeSeries.class,
                        config
                );
            }
        }
    }

    private class SelectionRootContext extends AbstractUIRootContext {

        public SelectionRootContext(TimeSeriesServerDictionary serverDictionary, DisplayNameCalculator displayNameCalculator) {
            super(displayNameCalculator);
            DownloadSeriesImportExportHandler h = new DownloadSeriesImportExportHandler(this, serverDictionary);
            setImportExportHandler(h);
        }

        protected ContextUpdatingBusListener createContextBusListener() {
            return new ContextUpdatingBusListener(this);
        }
    }

    private class ServerTimeSeriesFactory extends AbstractUIContextTimeSeriesFactory {
        protected UIPropertiesTimeSeries createTimeSeriesForConfig(UiTimeSeriesConfig config) throws MalformedURLException {
            return new ServerTimeSeries(config);
        }
    }

    private class DownloadSeriesImportExportHandler extends ContextImportExportHandler {

        public DownloadSeriesImportExportHandler(TimeSeriesContext rootContext, TimeSeriesServerDictionary serverDictionary) {
            super(rootContext);
            setContextFactory(new ServerContextCreatingContextFactory(rootContext, serverDictionary));
            setTimeSeriesFactory(new ServerTimeSeriesFactory());
        }

        protected boolean shouldImport(Identifiable i, Identifiable target) {
            return true;
        }

        protected boolean canImport(Identifiable i, Identifiable target) {
            return false;
        }

        protected ImportItem getImportItem(Component component, Identifiable identifiable, Identifiable target) {
            return null;
        }

        public int getSourceActions(IdentifiableListActionModel selected) {
            return DnDConstants.ACTION_NONE;
        }

        public void doExport(IdentifiableListActionModel transferData, int action) {
        }

    }
}
