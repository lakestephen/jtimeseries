package com.od.jtimeseries.ui.timeserious;

import com.od.jtimeseries.ui.config.CollectionClearingConfigAware;
import com.od.jtimeseries.ui.config.ConfigAwareTreeManager;
import com.od.jtimeseries.ui.config.ConfigInitializer;
import com.od.jtimeseries.ui.config.TimeSeriousConfig;
import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
import com.od.jtimeseries.ui.displaypattern.EditDisplayNamePatternsAction;
import com.od.jtimeseries.ui.net.udp.UiTimeSeriesServerDictionary;
import com.od.jtimeseries.ui.selector.SeriesSelectionPanel;
import com.od.jtimeseries.ui.timeserious.action.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 26-Mar-2010
 * Time: 15:14:34
 */
public class TimeSeriousMainFrame extends AbstractDesktopFrame implements CollectionClearingConfigAware {

    private JMenuBar mainMenuBar = new JMenuBar();
    private MainSeriesSelector mainSeriesSelector;
    private NewVisualizerAction newVisualizerAction;
    private NewDesktopAction newDesktopAction;
    private NewServerAction newServerAction;
    private EditDisplayNamePatternsAction editDisplayNamePatternsAction;
    private ConfigAwareTreeManager configTreeManager;
    private ConfigInitializer configInitializer;
    private DisplayNameCalculator displayNameCalculator;
    private UiTimeSeriesServerDictionary serverDictionary;
    private JSplitPane splitPane = new JSplitPane();
    private int tableSplitPanePosition;
    private int treeSplitPanePosition;
    private ExitAction exitAction;
    private ExportConfigAction exportConfigAction;
    private ImportConfigAction importConfigAction;

    public TimeSeriousMainFrame(UiTimeSeriesServerDictionary serverDictionary, ApplicationActionModels actionModels, ConfigAwareTreeManager configTreeManager, ConfigInitializer configInitializer, DisplayNameCalculator displayNameCalculator, TimeSeriousRootContext rootContext, MainSeriesSelector mainSeriesSelector, DesktopContext desktopContext) {
        super(serverDictionary, displayNameCalculator, desktopContext, mainSeriesSelector.getSelectionPanel(), rootContext, actionModels);
        this.serverDictionary = serverDictionary;
        this.configTreeManager = configTreeManager;
        this.configInitializer = configInitializer;
        this.displayNameCalculator = displayNameCalculator;
        this.mainSeriesSelector = mainSeriesSelector;
        createActions();
        initializeFrame();
        createMenuBar();
        createToolBar();
        layoutFrame();
        addListeners();
        addExitListener();
    }

    private void addExitListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!exitAction.confirmAndSaveConfig(e.getWindow())) {
                    //there's no mechanism to cancel the close which I can find, barring throwing an exception
                    //which is then handled by some dedicated logic in the Component class
                    throw new RuntimeException("User cancelled exit");
                }
            }
        });
    }

    private void createActions() {
        exitAction = new ExitAction(this, configTreeManager, configInitializer);

        exportConfigAction = new ExportConfigAction(this, configTreeManager, configInitializer);

        importConfigAction = new ImportConfigAction(this, configTreeManager, configInitializer);

        newVisualizerAction = new NewVisualizerAction(
            this,
            getActionModels().getDesktopSelectionActionModel(),
            getActionModels().getVisualizerSelectionActionModel()
        );
        newServerAction = new NewServerAction(this, serverDictionary);
        editDisplayNamePatternsAction = new EditDisplayNamePatternsAction(
            TimeSeriousMainFrame.this,
            displayNameCalculator
        );
        newDesktopAction = new NewDesktopAction(this, getRootContext(), getActionModels().getDesktopSelectionActionModel());
    }

    private void addListeners() {
        addSplitLocationListener();
        addSelectorChangeListener();
    }

    private void addSplitLocationListener() {
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( mainSeriesSelector.isTableSelectorVisible()) {
                    tableSplitPanePosition = (Integer)evt.getNewValue();
                } else {
                    treeSplitPanePosition = (Integer)evt.getNewValue();
                }
            }
        });
    }

    private void addSelectorChangeListener() {
        //set the split pane position when we change between tree and table view
        mainSeriesSelector.addPropertyChangeListener(SeriesSelectionPanel.TREE_VIEW_SELECTED_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        splitPane.setDividerLocation(
                                ((Boolean) evt.getNewValue()) ? treeSplitPanePosition : tableSplitPanePosition
                        );
                    }
                });
    }

    protected Component getMainComponent() {
        Component c = splitPane;
        splitPane.setLeftComponent(mainSeriesSelector);
        splitPane.setRightComponent(getDesktopPane());
        return c;
    }

    private void createToolBar() {
        getToolBar().add(newDesktopAction);
        getToolBar().add(newVisualizerAction);
        getToolBar().add(newServerAction);
        getToolBar().add(editDisplayNamePatternsAction);
    }

    private void initializeFrame() {
        setTitle("TimeSerious");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(splitPane.getDividerSize() + 4);
    }

    private void createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setOpaque(false);
        mainMenuBar.add(fileMenu);

        JMenuItem newServerItem = new JMenuItem(newServerAction);
        fileMenu.add(newServerItem);

        JMenuItem exportConfigItem = new JMenuItem(exportConfigAction);
        fileMenu.add(exportConfigItem);

        JMenuItem importConfigItem = new JMenuItem(importConfigAction);
        fileMenu.add(importConfigItem);

        JMenuItem exitItem = new JMenuItem(exitAction);
        fileMenu.add(exitItem);

        JMenu windowMenu = new JMenu("Window");
        JMenuItem newVisualizerItem = new JMenuItem(newVisualizerAction);
        windowMenu.add(newVisualizerItem);

        windowMenu.setOpaque(false);
        mainMenuBar.add(windowMenu);
        setJMenuBar(mainMenuBar);
    }

    public void restoreConfig(TimeSeriousConfig config) {
        splitPane.setDividerLocation(mainSeriesSelector.isTableSelectorVisible() ?
            config.getSplitPaneLocationWhenTableSelected() :
            config.getSplitPaneLocationWhenTreeSelected());
        tableSplitPanePosition = config.getSplitPaneLocationWhenTableSelected();
        treeSplitPanePosition = config.getSplitPaneLocationWhenTreeSelected();
    }

    public java.util.List<CollectionClearingConfigAware> getConfigAwareChildren() {
        return Collections.emptyList();
    }

    public void clearConfig() {
    }

    public void prepareConfigForSave(TimeSeriousConfig config) {
        config.setSplitPaneLocationWhenTreeSelected(treeSplitPanePosition);
        config.setSplitPaneLocationWhenTableSelected(tableSplitPanePosition);
    }

}
