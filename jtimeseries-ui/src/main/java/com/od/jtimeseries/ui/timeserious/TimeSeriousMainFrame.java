package com.od.jtimeseries.ui.timeserious;

import com.od.jtimeseries.ui.displaypattern.EditDisplayNamePatternsAction;
import com.od.jtimeseries.ui.event.TimeSeriousBusListener;
import com.od.jtimeseries.ui.net.udp.UiTimeSeriesServerDictionary;
import com.od.jtimeseries.ui.selector.SeriesSelectionPanel;
import com.od.jtimeseries.ui.timeserious.action.ApplicationActionModels;
import com.od.jtimeseries.ui.timeserious.action.DesktopSelectionActionModel;
import com.od.jtimeseries.ui.timeserious.action.NewServerAction;
import com.od.jtimeseries.ui.timeserious.action.NewVisualizerAction;
import com.od.jtimeseries.ui.timeserious.config.ConfigAware;
import com.od.jtimeseries.ui.timeserious.config.TimeSeriousConfig;
import com.od.jtimeseries.ui.util.ImageUtils;
import com.od.swing.eventbus.EventSender;
import com.od.swing.eventbus.UIEventBus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 26-Mar-2010
 * Time: 15:14:34
 */
public class TimeSeriousMainFrame extends JFrame implements ConfigAware {

    public static final String MAIN_FRAME_NAME = "mainFrame";

    private TimeSeriousRootContext rootContext;
    private JMenuBar mainMenuBar = new JMenuBar();
    private DesktopPanel desktopPanel;
    private MainSeriesSelector seriesSelector;
    private JToolBar mainToolBar = new JToolBar();
    private DesktopSelectionActionModel desktopSelectionActionModel;
    private NewVisualizerAction newVisualizerAction;
    private NewServerAction newServerAction;
    private EditDisplayNamePatternsAction editDisplayNamePatternsAction;
    private UiTimeSeriesServerDictionary serverDictionary;
    private final JSplitPane splitPane = new JSplitPane();
    private int tableSplitPanePosition;
    private int treeSplitPanePosition;

    public TimeSeriousMainFrame(UiTimeSeriesServerDictionary serverDictionary, ApplicationActionModels actionModels) {
        this.serverDictionary = serverDictionary;
        this.rootContext = new TimeSeriousRootContext(serverDictionary);
        this.desktopPanel = new DesktopPanel(serverDictionary);
        this.seriesSelector = new MainSeriesSelector(
            rootContext,
            actionModels,
            rootContext.getDisplayNameCalculator(),
            serverDictionary
        );
        createActions(actionModels);
        initializeFrame();
        createMenuBar();
        createToolBar();
        layoutFrame();
        addListeners();
    }

    private void createActions(ApplicationActionModels actionModels) {
        desktopSelectionActionModel = actionModels.getDesktopSelectionActionModel();
        newVisualizerAction = new NewVisualizerAction(this, desktopSelectionActionModel);
        newServerAction = new NewServerAction(this, rootContext, serverDictionary);
        editDisplayNamePatternsAction = new EditDisplayNamePatternsAction(
            rootContext,
            TimeSeriousMainFrame.this,
            rootContext.getDisplayNameCalculator()
        );
    }

    private void addListeners() {
        addWindowFocusListener(new DesktopSelectionWindowFocusListener());
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( seriesSelector.isTableSelectorVisible()) {
                    tableSplitPanePosition = (Integer)evt.getNewValue();
                } else {
                    treeSplitPanePosition = (Integer)evt.getNewValue();
                }
            }
        });
    }

    private void addSplitPaneListener() {
        //set the split pane position when we change between tree and table view
        seriesSelector.addPropertyChangeListener(SeriesSelectionPanel.TREE_VIEW_SELECTED_PROPERTY,
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                splitPane.setDividerLocation(
                    ((Boolean)evt.getNewValue()) ? treeSplitPanePosition : tableSplitPanePosition
                );
            }
        });
    }

    private void layoutFrame() {
        setJMenuBar(mainMenuBar);
        splitPane.setLeftComponent(seriesSelector);
        splitPane.setRightComponent(desktopPanel);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        add(mainToolBar, BorderLayout.NORTH);
    }

    private void createToolBar() {
        mainToolBar.add(newVisualizerAction);
        mainToolBar.add(newServerAction);
        mainToolBar.add(editDisplayNamePatternsAction);
    }

    private void initializeFrame() {
        setTitle("TimeSerious");
        setIconImage(ImageUtils.FRAME_ICON_16x16.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        mainMenuBar.add(fileMenu);

        JMenuItem newServerItem = new JMenuItem(newServerAction);
        fileMenu.add(newServerItem);

        JMenuItem exitItem = new JMenuItem(new ExitAction());
        fileMenu.add(exitItem);

        JMenu windowMenu = new JMenu("Window");
        JMenuItem newVisualizerItem = new JMenuItem(newVisualizerAction);
        windowMenu.add(newVisualizerItem);

        mainMenuBar.add(windowMenu);
    }

    public void restoreConfig(TimeSeriousConfig config) {
        Rectangle frameLocation = config.getFrameLocation(MAIN_FRAME_NAME);
        if ( frameLocation != null) {
            setBounds(frameLocation);
            setExtendedState(config.getFrameExtendedState(MAIN_FRAME_NAME));
        } else {
            setSize(800, 600);
            setLocationRelativeTo(null);
        }
        splitPane.setDividerLocation(seriesSelector.isTableSelectorVisible() ?
            config.getSplitPaneLocationWhenTableSelected() :
            config.getSplitPaneLocationWhenTreeSelected());
        tableSplitPanePosition = config.getSplitPaneLocationWhenTableSelected();
        treeSplitPanePosition = config.getSplitPaneLocationWhenTreeSelected();
        addSplitPaneListener();
    }

    public java.util.List<ConfigAware> getConfigAwareChildren() {
        return Arrays.asList(desktopPanel, rootContext, seriesSelector);
    }

    public void prepareConfigForSave(TimeSeriousConfig config) {
        config.setFrameLocation(MAIN_FRAME_NAME, getBounds());
        config.setFrameExtendedState(MAIN_FRAME_NAME, getExtendedState());
        config.setSplitPaneLocationWhenTreeSelected(treeSplitPanePosition);
        config.setSplitPaneLocationWhenTableSelected(tableSplitPanePosition);
    }

    public DesktopPanel getSelectedDesktop() {
        return desktopPanel;
    }

    private class ExitAction extends AbstractAction {

        private ExitAction() {
            super("Exit");
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    //set the selected desktop in the desktopSelectionActionModel when this window is focused
    private class DesktopSelectionWindowFocusListener implements WindowFocusListener {

        public void windowGainedFocus(WindowEvent e) {
            UIEventBus.getInstance().fireEvent(TimeSeriousBusListener.class,
                new EventSender<TimeSeriousBusListener>() {
                    public void sendEvent(TimeSeriousBusListener listener) {
                        listener.desktopSelected(desktopPanel);
                    }
                }
            );
        }

        public void windowLostFocus(WindowEvent e) {
        }
    }
}
