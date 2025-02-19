/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.tape.impl;

import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.io.Serial;

/**
 * A {@link JTapeTab} is composed of a title and a set of {@link JTapeGroup}.
 * Each {@link JTapeTab} contains a hidden {@link JProgressBar} too.
 * Features with a common context should appear in the same tab.
 * E.g., Common operations
 * like New, Open, Close, Save, etc. should stay together.
 *
 * @deprecated The entire Swing UI is deprecated and planned to be replaced with a modern UI.
 */
@Deprecated
public class JTapeTab extends JPanel {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -5784994713230091928L;
    private static final String GROW_FILL = "[grow,fill]";
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
        contentPanel.setLayout(new MigLayout("", "", GROW_FILL));

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
            contentPanel.setLayout(new MigLayout("", layoutString, GROW_FILL));
            contentPanel.add(new JSeparator(SwingConstants.VERTICAL), "cell " + compCount++ + " 0");
        }
        layoutString = layoutString + "[fill]";
        contentPanel.setLayout(new MigLayout("", layoutString, GROW_FILL));
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
