/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.tape;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

/**
 * A {@link JTapeTab} is composed of a title and a set of {@link JTapeGroup}.
 * Each {@link JTapeTab} contains an hidden {@link JProgressBar} too. Features
 * with a common context should appear into the same tab. E.g. Common operations
 * like New, Open, Close, Save, etc. should stay togheter.
 * 
 */
public class JTapeTab extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = -5784994713230091928L;
    private String title;
    private String layoutString = "";
    private JProgressBar progressBar;
    private final JPanel contentPanel;
    private int compCount;

    /**
     * Initializes a new {@link JTapeTab} with the title in input.
     * 
     * @param t
     *            is the {@link String} containing the title
     */
    public JTapeTab(final String t) {
        super();
        setLayout(new BorderLayout(0, 0));

        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.SOUTH);
        progressBar.setVisible(false);

        contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "", "[grow,fill]"));

        title = t;
    }

    /**
     * Lets child-classes access the progress bar.
     * 
     * @return a {@link JProgressBar} instance
     */
    protected JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Gets the title.
     * 
     * @return a {@link String}
     */
    public String getTitle() {
        return title;
    }

    /**
     * Adds a group of features to the tab.
     * 
     * @param g
     *            is the {@link JTapeGroup} to add
     * @return <code>true</code>
     */
    public boolean registerGroup(final JTapeGroup g) {
        if (compCount > 0) {
            layoutString = layoutString + "[fill]";
            contentPanel.setLayout(new MigLayout("", layoutString, "[grow,fill]"));
            contentPanel.add(new JSeparator(SwingConstants.VERTICAL), "cell " + compCount++ + " 0");
        }
        layoutString = layoutString + "[fill]";
        contentPanel.setLayout(new MigLayout("", layoutString, "[grow,fill]"));
        contentPanel.add(g, "cell " + compCount++ + " 0");
        return true;
    }

    /**
     * Lets child-classes change the progress bar.
     * 
     * @param pb
     *            is the new {@link JProgressBar} instance
     */
    protected void setProgressBar(final JProgressBar pb) {
        progressBar = pb;
    }

    /**
     * Sets the title.
     * 
     * @param t
     *            is a {@link String}
     */
    public void setTitle(final String t) {
        title = t;
    }

}
