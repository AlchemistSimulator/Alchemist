/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.asmc;

import javax.swing.JPanel;

/**
 * A chart panel that, given a series of XY values, each comprised in a
 * surrounding interval, plots series line and interval area.
 * 
 */
public abstract class ASMCPlot extends JPanel {

    private static final long serialVersionUID = -7436019965350494058L;

    /**
     * @param values
     *            A plot-ready array of quadruples: (x-value, y-lb, y-value,
     *            x-lb)
     * @param lb
     *            A lower bound for plotting
     * @param ub
     *            An upper bound for plotting
     * @param sampleSize
     *            How many simulations have run
     * 
     */
    public abstract void batchDone(double[][] values, double lb, double ub, int sampleSize);

    /**
     * Toggles between available views, when available.
     */
    public abstract void switchView();
}
