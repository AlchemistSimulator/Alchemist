package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.input.KeyboardActionListener;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.util.Collection;
import java.util.List;
import javafx.scene.canvas.Canvas;

/**
 * {@code OutputMonitor} that handles the graphical part of the simulation in JavaFX.
 *
 * @param <T> the {@link it.unibo.alchemist.model.interfaces.Concentration} type
 * @param <P> the position type
 */
public interface FXOutputMonitor<T, P extends Position2D<? extends P>> extends OutputMonitor<T, P> {

    /**
     * Getter method for the steps.
     *
     * @return how many simulation steps this monitor updates the graphics
     */
    int getStep();

    /**
     * Setter method fo the steps.
     *
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
     * @param realTime true for the real time mode
     */
    void setRealTime(boolean realTime);

    /**
     * Repaints this {@link Canvas}' {@link javafx.scene.canvas.GraphicsContext} by drawing all the {@link it.unibo.alchemist.boundary.gui.effects.EffectFX Effect}s of each
     * {@link it.unibo.alchemist.model.interfaces.Node} of the specified {@link it.unibo.alchemist.model.interfaces.Environment}.
     */
    void repaint();

    /**
     * Getter method for the {@link it.unibo.alchemist.boundary.gui.effects.EffectFX Effects} to draw.
     *
     * @return the current {@code Effects} to draw
     */
    Collection<EffectGroup<P>> getEffects();

    /**
     * Setter method for the effects to draw.
     * <p>
     * All previous set {@link it.unibo.alchemist.boundary.gui.effects.EffectFX Effects} are removed.
     *
     * @param effects the {@code Effects} to draw
     */
    void setEffects(Collection<EffectGroup<P>> effects);

    /**
     * Add all the {@link EffectGroup}s in the collection to the {@link it.unibo.alchemist.boundary.gui.effects.EffectFX Effects} to draw.
     *
     * @param effects the {@link EffectGroup}s to draw
     * @see Collection#addAll(Collection)
     */
    void addEffects(Collection<EffectGroup<P>> effects);

    /**
     * Add the {@link EffectGroup} in the collection to the {@link it.unibo.alchemist.boundary.gui.effects.EffectFX Effects} to draw.
     *
     * @param effects the {@link EffectGroup} to draw
     * @see Collection#add(Object)
     */
    void addEffectGroup(EffectGroup<P> effects);

    /**
     * Getter method for the current view status.
     *
     * @return the current {@code ViewStatus}
     */
    ViewStatus getViewStatus();

    /**
     * Setter method for the current view status.
     *
     * @param viewStatus the {@code ViewStatus} to set
     */
    void setViewStatus(ViewStatus viewStatus);

    /**
     * The enum models the status of the view.
     */
    enum ViewStatus {
        /** In this status, click and drag to move the view. */
        PANNING,
        /** In this status, click and drag to select nodes. */
        SELECTING,
        /** In this status, click and drag to move selected nodes. */
        MOVING,
        /** In this status, click to clone nodes. */
        CLONING,
        /** In this status, click to delete nodes. */
        DELETING
    }

    /**
     * Returns the keyboard listener associated with this monitor.
     * @return the listener
     */
    KeyboardActionListener getKeyboardListener();
}
