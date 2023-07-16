/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.api;

import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.model.Position;

import java.util.List;

/**
 * {@code OutputMonitor} that handles the graphical part of the simulation.
 *
 * @param <P> position type
 * @param <T> concentration type
 */
@Deprecated
public interface GraphicalOutputMonitor<T, P extends Position<? extends P>> extends OutputMonitor<T, P> {

    /**
     * @return how many simulation steps this monitor updates the graphics
     */
    int getStep();

    /**
     * @param step How many steps should be computed by the engine for the
     *             display to update the graphics
     */
    void setStep(int step);

    /**
     * @return true if this monitor is trying to draw in realtime
     */
    boolean isRealTime();

    /**
     * If enabled, the monitor tries to synchronize the simulation time with the
     * real time, slowing down the simulator if needed. If the simulation is
     * slower than the real time, then the display refreshes fast enough to keep
     * the default frame rate.
     *
     * @param rt true for the real time mode
     */
    void setRealTime(boolean rt);

    /**
     * Repaints the GUI.
     */
    void repaint();

    /**
     * @param b if true, this monitor draws the links between nodes
     */
    void setDrawLinks(boolean b);

    /**
     * @param l the Effect stack to use
     */
    void setEffectStack(List<Effect> l);

    /**
     * If set, the node closer to the mouse will be put in evidence.
     *
     * @param mark true if the node closer to the mouse should be marked
     */
    void setMarkCloserNode(boolean mark);
}
