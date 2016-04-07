/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.asmc;

/**
 * A Sampler for the probability v.s. time function (see Manual), returning both
 * mean value and confidence interval.
 * 
 */
public interface ASMCSampler {

    /**
     * Start a new evaluation by passing a new set of values to the Sampler. At
     * the end, the graph is repainted.
     * 
     * @param values
     *            An ASCENDANT ORDERED array of time values
     */
    void batchDone(Double[] values);

    /**
     * Re-computes and redraws.
     */
    void redraw();

    /**
     * Causes the graph to scale to best-fit zooming level.
     */
    void setAutoScale();

    /**
     * @param lowerBound
     *            The lower end of graph domain
     * @param upperBound
     *            The upper end of graph domain
     */
    void setBounds(double lowerBound, double upperBound);

    /**
     * @param alpha
     *            Confidence value
     */
    void setConfidence(double alpha);

    /**
     * @param grain
     *            The step
     */
    void setGranularity(double grain);

    /**
     * @param lowerBound
     *            The lower end of graph domain
     */
    void setLowerBound(double lowerBound);

    /**
     * Sets the plotter associated to this sampler.
     * 
     * @param sp
     *            the Plotter
     */
    void setPlotter(ASMCPlot sp);

    /**
     * @param upperBound
     *            The upper end of graph domain
     */
    void setUpperBound(double upperBound);
}