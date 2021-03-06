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
package com.od.jtimeseries.ui.timeserious.mainselector;

import com.od.jtimeseries.net.udp.TimeSeriesServerDictionary;
import com.od.jtimeseries.ui.config.ColumnSettings;
import com.od.jtimeseries.ui.config.ConfigAware;
import com.od.jtimeseries.ui.config.TimeSeriousConfig;
import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
import com.od.jtimeseries.ui.selector.SeriesSelectionPanel;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
import com.od.jtimeseries.ui.timeserious.action.ApplicationActionModels;
import com.od.jtimeseries.ui.timeserious.rootcontext.TimeSeriousRootContext;
import com.od.jtimeseries.ui.uicontext.ImportExportTransferHandler;
import com.od.swing.util.ProxyingPropertyChangeListener;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 24-Nov-2010
 * Time: 09:36:25
 */
public class MainSeriesSelector extends JPanel implements ConfigAware {

    private SeriesSelectionPanel<UIPropertiesTimeSeries> selectionPanel;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private DisplayNameCalculator displayNameCalculator;

    public MainSeriesSelector(TimeSeriousRootContext rootContext, ApplicationActionModels applicationActionModels, TimeSeriesServerDictionary dictionary, DisplayNameCalculator displayNameCalculator) {
        this.displayNameCalculator = displayNameCalculator;
        selectionPanel = new SeriesSelectionPanel<UIPropertiesTimeSeries>(
            rootContext,
            UIPropertiesTimeSeries.class,
            new MainSelectorTreeNodeFactory(UIPropertiesTimeSeries.class)
        );

        //don't enable selection of series for charting, that's not the purpose of the main selector
        selectionPanel.setSeriesSelectionEnabled(false);

        selectionPanel.setTreeComparator(new MainSelectorTreeComparator());

        MainSelectorActionFactory actionFactory = new MainSelectorActionFactory(
            rootContext,
            applicationActionModels,
            selectionPanel,
            dictionary,
            displayNameCalculator,
            this
        );

        selectionPanel.setActionFactory(actionFactory);
        addProxyingPropertyListeners();

        selectionPanel.setTransferHandler(new ImportExportTransferHandler(rootContext,
                selectionPanel.getSelectionActionModel()));

        setLayout(new BorderLayout());
        add(selectionPanel, BorderLayout.CENTER);
    }

    private void addProxyingPropertyListeners() {
        //allow clients to subscribe to the main selector to receive
        //tree view selected events from the selector panel
        selectionPanel.addPropertyChangeListener(
            SeriesSelectionPanel.TREE_VIEW_SELECTED_PROPERTY,
            new ProxyingPropertyChangeListener(
                propertyChangeSupport
            )
        );
    }

    public boolean isTableSelectorVisible() {
        return selectionPanel.isTableSelectorVisible();
    }

    public void prepareConfigForSave(TimeSeriousConfig config) {
        config.setMainSeriesSelectorTableVisible(selectionPanel.isTableSelectorVisible());
        config.setMainSelectorColumnSettings(selectionPanel.getColumnSettings());
    }

    public void restoreConfig(TimeSeriousConfig config) {
        selectionPanel.setTableSelectorVisible(config.isMainSeriesSelectorTableVisible());
        List<ColumnSettings> mainSelectorColumnSettings = config.getMainSelectorColumnSettings();
        if ( mainSelectorColumnSettings != null) {
            selectionPanel.setColumnSettings(mainSelectorColumnSettings);
        }
    }

    public List<ConfigAware> getConfigAwareChildren() {
        return Collections.emptyList();
    }

    public void clearConfig() {
    }

    public SeriesSelectionPanel<UIPropertiesTimeSeries> getSelectionPanel() {
        return selectionPanel;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

}
