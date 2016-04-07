/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.danilopianini.view.ButtonTabComponent;
import org.danilopianini.view.GUIUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibo.alchemist.boundary.l10n.R.getString;

/**
 * The main frame for the standard Alchemist GUI.
 * 
 * 
 */
public final class AlchemistSwingUI extends JFrame {

    /**
     * The default icon size.
     */
    public static final byte DEFAULT_ICON_SIZE = 16;

    /**
     * The default window dimension.
     */
    public static final int DEFAULT_WINDOW_HEIGHT = 960, DEFAULT_WINDOW_WIDTH = 1280;
    private static final JTabbedPane MAINPANE = new JTabbedPane(SwingConstants.BOTTOM);
    private static final long serialVersionUID = -5060969438004075630L;
    private static final Logger L = LoggerFactory.getLogger(AlchemistSwingUI.class);

    private final JMenu[] menus = { new FileMenu(), new Menu() };

    /**
     * Inserts a new tab for the given component, at the given index,
     * represented by the given title and/or icon, either of which may be
     * {@code null}.
     * 
     * @param c
     *            the component to be displayed when this tab is clicked.
     */
    public static void addTab(final Component c) {
        addTab(c, c.getName());
    }

    /**
     * Inserts a new tab for the given component, at the given index,
     * represented by the given title and/or icon, either of which may be
     * {@code null}.
     * 
     * @param c
     *            the component to be displayed when this tab is clicked.
     * @param i
     *            the icon to be displayed on the tab
     * @param tabName
     *            the title to be displayed on the tab
     * @param tip
     *            the tooltip to be displayed for this tab
     */
    public static void addTab(final Component c, final Icon i, final String tabName, final String tip) {
        addTab(tabName, i, c, tip, 0);
    }

    /**
     * Inserts a new tab for the given component, at the given index,
     * represented by the given title and/or icon, either of which may be
     * {@code null}.
     * 
     * @param c
     *            the component to be displayed when this tab is clicked.
     * @param tabName
     *            the title to be displayed on the tab
     */
    public static void addTab(final Component c, final String tabName) {
        addTab(c, tabName, getString("no_description_available"));
    }

    /**
     * Inserts a new tab for the given component, at the given index,
     * represented by the given title and/or icon, either of which may be
     * {@code null}.
     * 
     * @param c
     *            the component to be displayed when this tab is clicked.
     * @param tabName
     *            the title to be displayed on the tab
     * @param tip
     *            the tooltip to be displayed for this tab
     */
    public static void addTab(final Component c, final String tabName, final String tip) {
        addTab(tabName, null, c, tip, 0);
    }

    /**
     * Inserts a new tab for the given component, at the given index,
     * represented by the given title and/or icon, either of which may be
     * {@code null}.
     * 
     * @param tabName
     *            the title to be displayed on the tab
     * @param i
     *            the icon to be displayed on the tab
     * @param c
     *            the component to be displayed when this tab is clicked.
     * @param tip
     *            the tooltip to be displayed for this tab
     * @param index
     *            the position to insert this new tab (
     *            {@code > 0 and <= getTabCount()})
     * 
     */
    public static void addTab(final String tabName, final Icon i, final Component c, final String tip, final int index) {
        MAINPANE.insertTab(tabName, i, c, tip, index);
        final ActionListener al = c instanceof ActionListener ? (ActionListener) c : null;
        MAINPANE.setTabComponentAt(0, new ButtonTabComponent(MAINPANE, al));
    }

    /**
     * Loads an image and scales it to the default Alchemist's icon size.
     * 
     * @param p
     *            the path where to load the image. The system resource loader
     *            is used to do so, with all its advantages
     * @return the resized icon
     */
    public static ImageIcon loadScaledImage(final String p) {
        ImageIcon res = GUIUtilities.loadScaledImage(p, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE);
        if (res == null) {
            res = GUIUtilities.loadScaledImage("/resources" + p, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE);
        }
        return res;
    }

    /**
     * Loads an image and scales it to the desired size.
     * 
     * @param p
     *            the path where to load the image. The system resource loader
     *            is used to do so, with all its advantages
     * 
     * @param size
     *            the size which will be used both for x and y axes
     * @return the resized icon
     */
    public static ImageIcon loadScaledImage(final String p, final int size) {
        ImageIcon res = GUIUtilities.loadScaledImage(p, size, size);
        if (res == null) {
            res = GUIUtilities.loadScaledImage("/resources" + p, size, size);
        }
        return res;
    }

    /**
     * @param args
     *            No arguments needed so far.
     */
    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            L.error("Unable to set the system look and feel", e);
        }
        final AlchemistSwingUI f = new AlchemistSwingUI();
        GUIUtilities.packAndDisplayInCenterOfScreen(f);
        f.setExtendedState(MAXIMIZED_BOTH);
        f.setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
    }

    // private final OptionsSet options;

    /**
     * Builds a new GUI instance.
     */
    public AlchemistSwingUI() {
        super(getString("alchemist"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel pane = new JPanel();
        getContentPane().add(pane);
        pane.setLayout(new BorderLayout());
        final JMenuBar bar = new JMenuBar();
        pane.add(bar, BorderLayout.NORTH);
        for (final JMenu m : menus) {
            bar.add(m);
        }
        pane.add(MAINPANE, BorderLayout.CENTER);
        setExtendedState(MAXIMIZED_BOTH);
    }

}
