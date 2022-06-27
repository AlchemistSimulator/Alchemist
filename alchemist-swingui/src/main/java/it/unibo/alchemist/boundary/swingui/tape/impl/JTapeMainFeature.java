/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.tape.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.swing.SpringLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 *
 */
@Deprecated
@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "This class is not meant to get serialized")
public final class JTapeMainFeature extends JTapeSection {

    /**
     * 
     */
    private static final long serialVersionUID = -3756166558195051145L;
    private final SpringLayout springLayout;

    /**
     * 
     */
    public JTapeMainFeature() {
        super();

        setBackground(Color.YELLOW);
        springLayout = new SpringLayout();

        setLayout(springLayout);
    }

    /**
     * 
     */
    @Override
    public boolean registerFeature(final Component c) {
        if (getComponentCount() != 0) {
            return false;
        }
        final Dimension d = c.getPreferredSize();
        springLayout.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, c, 0, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, c, 0, SpringLayout.EAST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, c, 0, SpringLayout.SOUTH, this);
        setMinimumSize(d);
        add(c);
        return true;
    }

    @Override
    public boolean unregisterFeature(final Component c) {
        return false;
    }

}
