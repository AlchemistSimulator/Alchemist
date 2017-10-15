package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;

import java.util.Collection;

/**
 * {@code OutputMonitor} that handles the graphical part of the simulation in JavaFX.
 *
 * @param <T> the {@link Concentration} type
 */
public interface FXOutputMonitor<T> extends OutputMonitor<T> {
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
     * @return true if the node closer to the mouse should be marked
     */
    boolean isMarkCloserNode();

    /**
     * If set, the node closer to the mouse will be put in evidence.
     *
     * @param mark true if the node closer to the mouse should be marked
     */
    void setMarkCloserNode(boolean mark);

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
     * @param realTime true for the real time mode
     */
    void setRealTime(boolean realTime);

    /**
     * Repaints the GUI.
     *
     * @param environment the {@link Environment} to repaint
     */
    void repaintEnvironment(Environment<T> environment);

    /**
     * @param effects the Effect stack to use
     */
    void setEffects(Collection<EffectGroup> effects);
}
