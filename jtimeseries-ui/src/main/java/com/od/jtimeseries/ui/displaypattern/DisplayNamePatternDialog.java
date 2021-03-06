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
package com.od.jtimeseries.ui.displaypattern;

import com.od.jtimeseries.ui.config.DisplayNamePattern;
import com.od.jtimeseries.ui.config.DisplayNamePatternConfig;
import com.od.jtimeseries.ui.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 01-Jun-2009
 * Time: 10:49:24
 */
public class DisplayNamePatternDialog extends JDialog {

    private List<DisplayPatternListener> displayPatternListeners = new ArrayList<DisplayPatternListener>();
    private DisplayNamePatternTable table;
    private static final int DIALOG_WIDTH = 400;

    public DisplayNamePatternDialog(DisplayNamePatternConfig patterns) {
        setTitle("Edit Display Name Patterns");
        setAlwaysOnTop(true);
        setModal(false);
        setSize(DIALOG_WIDTH, 600);
        addComponents(patterns.getDisplayNamePatterns());
        setIconImage(ImageUtils.DISPLAY_NAME_16x16.getImage());
    }

    public void addDisplayPatternListener(DisplayPatternListener l) {
        displayPatternListeners.add(l);
    }

    public void removeDisplayPatternListener(DisplayPatternListener l) {
        displayPatternListeners.remove(l);
    }

    private void addComponents(List<DisplayNamePattern> patterns) {
        table = new DisplayNamePatternTable(patterns);

        JButton applyNowButton = new JButton(new ApplyNowAction());
        JButton okButton = new JButton(new OkAction());
        JButton cancelButton = new JButton(new CancelAction());

        JComponent descriptionPanel = createDescriptionPanel();

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(applyNowButton);
        buttonBox.add(Box.createHorizontalStrut(5));
        buttonBox.add(okButton);
        buttonBox.add(cancelButton);
        buttonBox.setBorder(new EmptyBorder(5,5,5,5));

        setLayout(new BorderLayout());
        add(descriptionPanel, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
        add(buttonBox, BorderLayout.SOUTH);
    }

    private JComponent createDescriptionPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JEditorPane e = new JEditorPane();
        e.setContentType("text/html");
        e.setText("<html><font color='blue' size='-1'>" +
            "Here you can set up regular expressions to calculate display names " +
            "for your timeseries. The expressions are applied to the tree path for " +
            "the series, so you can use capturing groups to include elements of the " +
            "tree path in the display name.</font></html>");
        //label.setPreferredSize(new Dimension(DIALOG_WIDTH - 15, 95));
        p.add(e, BorderLayout.CENTER);
        return p;
    }

    private class OkAction extends AbstractAction {
        public OkAction() {
            super("OK", ImageUtils.OK_16x16);
        }

        public void actionPerformed(ActionEvent e) {
            for ( DisplayPatternListener l : displayPatternListeners) {
                l.displayPatternsChanged(table.getDisplayPatterns(), false);
            }
            dispose();
        }
    }

    private class ApplyNowAction extends AbstractAction {
        public ApplyNowAction() {
            super("Apply Now", ImageUtils.DISPLAY_NAME_16x16);
        }

        public void actionPerformed(ActionEvent e) {
            for ( DisplayPatternListener l : displayPatternListeners) {
                l.displayPatternsChanged(table.getDisplayPatterns(), true);
            }
            table.repaint(); //failed status may have changed
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super("Cancel", ImageUtils.CANCEL_16x16);
        }

        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    public static interface DisplayPatternListener {
        void displayPatternsChanged(List<DisplayNamePattern> newPatterns, boolean applyNow);
    }
}
