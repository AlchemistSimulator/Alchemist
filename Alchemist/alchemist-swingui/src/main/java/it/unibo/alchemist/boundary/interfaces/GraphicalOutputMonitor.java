/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.boundary.gui.effects.Effect;

import java.util.List;

/**
 * @param <T>
 */
public interface GraphicalOutputMonitor<T> extends OutputMonitor<T> {

    /**
     * @return how many simulation steps this monitor updates the graphics
     */
    int getStep();

    /**
     * @return true if this monitor is trying to draw in realtime
     */
    boolean isRealTime();

    /**
     * 
     */
    void repaint();

    /**
     * @param b
     *            if true, this monitor draws the links between nodes
     */
    void setDrawLinks(boolean b);

    /**
     * @param l
     *            the Effect stack to use
     */
    void setEffectStack(List<Effect> l);

    /**
     * If enabled, the monitor tries to synchronize the simulation time with the
     * real time, slowing down the simulator if needed. If the simulation is
     * slower than the real time, then the display refreshes fast enough to keep
     * the default frame rate.
     * 
     * @param rt
     *            true for the real time mode
     */
    void setRealTime(boolean rt);

    /**
     * @param step
     *            How many steps should be computed by the engine for the
     *            display to update the graphics
     */
    void setStep(int step);

    /**
     * If set, the node closer to the mouse will be put in evidence.
     * 
     * @param mark
     *            true if the node closer to the mouse should be marked
     */
    void setMarkCloserNode(boolean mark);

}
