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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.Serial;

/**
 * A {@link JTapeGroup} is a set of {@link AbstractJTapeSection} identified by a common
 * description.
 *
 * @deprecated The entire Swing UI is deprecated and planned to be replaced with a modern UI.
 */
@Deprecated
public class JTapeGroup extends JPanel {

    @Serial
    private static final long serialVersionUID = 1066617411250906275L;
    private static final int DESC_TEXT_SIZE = 8;
    private final JLabel lblDescription;
    private final JPanel contentPanel;
    private String layoutString = "";

    /**
     * Initializes a new {@link JTapeGroup} with the description in input.
     *
     * @param d
     *            is a {@link String}
     */
    public JTapeGroup(final String d) {
        super();
        setLayout(new BorderLayout(0, 0));

        lblDescription = new JLabel(d);
        lblDescription.setFont(new Font("Dialog", Font.BOLD, DESC_TEXT_SIZE));
        lblDescription.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblDescription, BorderLayout.SOUTH);

        contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "", "[grow,fill]"));
    }

    /**
     * Gets the description.
     *
     * @return a {@link String}
     */
    public String getDescription() {
        return lblDescription.getText();
    }

    /**
     * Adds a section to the current group.
     *
     * @param section
     *            is the {@link AbstractJTapeSection} to add
     * @return <code>true</code>
     */
    public boolean registerSection(final AbstractJTapeSection section) {
        layoutString = layoutString + "[fill]";
        contentPanel.setLayout(new MigLayout("", layoutString, "[grow,fill]"));
        contentPanel.add(section, "cell " + contentPanel.getComponentCount() + " 0,grow");
        return true;
    }

    /**
     * Sets the description.
     *
     * @param d
     *            is a {@link String}
     */
    public void setDescription(final String d) {
        lblDescription.setText(d);
    }
}
