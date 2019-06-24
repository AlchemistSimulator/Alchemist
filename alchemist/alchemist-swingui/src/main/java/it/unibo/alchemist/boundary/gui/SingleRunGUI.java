/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui;

import it.unibo.alchemist.boundary.gui.effects.EffectSerializationFactory;
import it.unibo.alchemist.boundary.gui.effects.JEffectsTab;
import it.unibo.alchemist.boundary.interfaces.GraphicalOutputMonitor;
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.monitors.Generic2DDisplay;
import it.unibo.alchemist.boundary.monitors.MapDisplay;
import it.unibo.alchemist.boundary.monitors.TimeStepMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for quickly creating non-reusable graphical interfaces.
 */
public final class SingleRunGUI {

    private static final Logger L = LoggerFactory.getLogger(SingleRunGUI.class);
    private static final float SCALE_FACTOR = 0.8f;
    private static final int FALLBACK_X_SIZE = 800;
    private static final int FALLBACK_Y_SIZE = 600;

    private SingleRunGUI() {
    }

    /**
     * Builds a single-use graphical interface.
     * 
     * @param sim
     *            the simulation for this GUI
     * @param <T>
     *            concentration type
     * @param <P>
     *            position type
     */
    public static <T, P extends Position2D<P>> void make(final Simulation<T, P> sim) {
        make(sim, (File) null, JFrame.EXIT_ON_CLOSE);
    }

    /**
     * 
     * @param sim
     *            the simulation for this GUI
     * @param closeOperation
     *            the type of close operation for this GUI
     * @param <T>
     *            concentration type
     * @param <P>
     *            position type
     */
    public static <T, P extends Position2D<P>> void make(final Simulation<T, P> sim, final int closeOperation) {
        make(sim, (File) null, closeOperation);
    }

    /**
     * Builds a single-use graphical interface.
     * 
     * @param sim
     *            the simulation for this GUI
     * @param effectsFile
     *            the effects file
     * @param <T>
     *            concentration type
     * @param <P>
     *            position type
     */
    public static <T, P extends Position2D<P>> void make(final Simulation<T, P> sim, final String effectsFile) {
        make(sim, new File(effectsFile), JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Builds a single-use graphical interface.
     * 
     * @param sim
     *            the simulation for this GUI
     * @param effectsFile
     *            the effects file
     * @param closeOperation
     *            the type of close operation for this GUI
     * @param <T>
     *            concentration type
     * @param <P>
     *            position type
     */
    public static <T, P extends Position2D<P>> void make(final Simulation<T, P> sim, final String effectsFile, final int closeOperation) {
        make(sim, new File(effectsFile), closeOperation);
    }

    private static void errorLoadingEffects(final Throwable e) {
        L.error(LocalizedResourceBundle.getString("cannot_load_effects"), e);
    }

    /**
     * Builds a single-use graphical interface.
     * 
     * @param sim
     *            the simulation for this GUI
     * @param effectsFile
     *            the effects file
     * @param closeOperation
     *            the type of close operation for this GUI
     * @param <T>
     *            concentration type
     * @param <P>
     *            position type
     */
    public static <T, P extends Position2D<P>> void make(final Simulation<T, P> sim, final File effectsFile, final int closeOperation) {
        @SuppressWarnings("unchecked") // Actually safe: MapEnvironment uses the same P type of MapDisplay
        final GraphicalOutputMonitor<T, P> main = Objects.requireNonNull(sim).getEnvironment() instanceof MapEnvironment
                ? (GraphicalOutputMonitor<T, P>) new MapDisplay<T>()
                : new Generic2DDisplay<>();
        final JFrame frame = new JFrame("Alchemist Simulator");
        frame.setDefaultCloseOperation(closeOperation);
        final JPanel canvas = new JPanel();
        frame.getContentPane().add(canvas);
        canvas.setLayout(new BorderLayout());
        canvas.add((Component) main, BorderLayout.CENTER);
        /*
         * Upper area
         */
        final JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.X_AXIS));
        canvas.add(upper, BorderLayout.NORTH);
        final JEffectsTab<T> effects = new JEffectsTab<>(main, false);
        if (effectsFile != null) {
            try {
                effects.setEffects(EffectSerializationFactory.effectsFromFile(effectsFile));
            } catch (IOException | ClassNotFoundException ex) {
                errorLoadingEffects(ex);
            }
        }
        upper.add(effects);
        final TimeStepMonitor<T, P> time = new TimeStepMonitor<>();
        sim.addOutputMonitor(time);
        upper.add(time);
        /*
         * Go on screen
         */
        // frame.pack();
        final Optional<Dimension> size = Arrays
                .stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                .map(GraphicsDevice::getDisplayMode).map(dm -> new Dimension(dm.getWidth(), dm.getHeight()))
                .min(Comparator.comparingDouble(SingleRunGUI::area));
        size.ifPresent(d -> d.setSize(d.getWidth() * SCALE_FACTOR, d.getHeight() * SCALE_FACTOR));
        frame.setSize(size.orElse(new Dimension(FALLBACK_X_SIZE, FALLBACK_Y_SIZE)));
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        /*
         * OutputMonitor's add to the sim must be done as the last operation
         */
        sim.addOutputMonitor(main);
    }

    private static double area(final Dimension d) {
        return d.getWidth() * d.getHeight();
    }

}
