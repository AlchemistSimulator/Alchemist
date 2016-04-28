/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.asmc;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

/**
 * An ASMCPlot that makes use of the JFreeChart library.
 * 
 */
public class SimplePlot extends ASMCPlot {
    /**
     * 
     */
    private static final long serialVersionUID = 254120260303757755L;
    private static final int N_RENDERERS = 2;
    private static final int TRE = 3;
    private int currentRenderer;
    private static final Dimension DIMENSION = new Dimension(1500, 810);

    /**
     * Default constructor.
     */
    public SimplePlot() {
        super();
        this.setLayout(new BorderLayout());
        final JPanel placeHolder = new JPanel();
        final JLabel wait = new JLabel("Please wait.");
        placeHolder.add(wait);
        this.add(placeHolder, BorderLayout.NORTH);
    }

    @Override
    public void batchDone(final double[][] values, final double lower, final double upper, final int sampleSize) {
        this.removeAll();
        final YIntervalSeries series = new YIntervalSeries("Probability of condition satisfaction vs. time");
        for (final double[] value : values) {
            series.add(value[0], value[1], value[2], value[TRE]);
        }
        final YIntervalSeriesCollection data = new YIntervalSeriesCollection();
        data.addSeries(series);
        final JFreeChart chart = ChartFactory.createXYLineChart("", "X", "Y", data, PlotOrientation.VERTICAL, true, true, false);
        XYItemRenderer renderer;
        switch (currentRenderer) {
        case 1:
            renderer = new YIntervalRenderer();
            break;
        case 0:
        default:
            renderer = new DeviationRenderer(true, false);
        }
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(renderer);
        plot.getDomainAxis().setLowerBound(lower);
        plot.getDomainAxis().setUpperBound(upper);
        plot.getRangeAxis().setUpperBound(1.0);
        plot.getRangeAxis().setLowerBound(0.0);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(DIMENSION);
        this.setLayout(new BorderLayout());
        this.add(chartPanel, BorderLayout.NORTH);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chartPanel.getRootPane().validate();
            }
        });
    }

    @Override
    public void switchView() {
        currentRenderer = ++currentRenderer % N_RENDERERS;
    }

}
