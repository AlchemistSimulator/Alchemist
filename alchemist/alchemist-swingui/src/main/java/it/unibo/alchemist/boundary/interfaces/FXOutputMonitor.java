package it.unibo.alchemist.boundary.interfaces;

import com.google.common.collect.ImmutableSet;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.util.Collection;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.jetbrains.annotations.NotNull;

/**
 * {@code OutputMonitor} that handles the graphical part of the simulation in JavaFX.
 *
 * @param <T> the {@link Concentration} type
 */
public interface FXOutputMonitor<T, P extends Position2D<?>> extends OutputMonitor<T, Position2D<?>> {

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
     * Repaints this {@link Canvas}' {@link GraphicsContext} by drawing all the {@link EffectFX Effect}s of each
     * {@link Node} of the specified {@link Environment}.
     */
    void repaint();

    /**
     * Getter method for the {@link EffectFX Effects} to draw.
     *
     * @return the current {@code Effects} to draw
     */
    Collection<EffectGroup> getEffects();

    /**
     * Setter method for the effects to draw.
     * <p>
     * All previous set {@link EffectFX Effects} are removed.
     *
     * @param effects the {@code Effects} to draw
     */
    void setEffects(Collection<EffectGroup> effects);

    /**
     * Add all the {@link EffectGroup}s in the collection to the {@link EffectFX Effects} to draw.
     *
     * @param effects the {@link EffectGroup}s to draw
     * @see Collection#addAll(Collection)
     */
    void addEffects(Collection<EffectGroup> effects);

    /**
     * Add the {@link EffectGroup} in the collection to the {@link EffectFX Effects} to draw.
     *
     * @param effects the {@link EffectGroup} to draw
     * @see Collection#add(Object)
     */
    void addEffectGroup(EffectGroup effects);

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
        /** In this status, click and drag to select nodes. */
        SELECTING,
        /** In this status, click and drag to move selected nodes. */
        MOVING,
        /** In this status, click to clone nodes. */
        CLONING,
        /** In this status, click to delete nodes. */
        DELETING,
        /** In this status, click to open editor to modify node properties. */
        EDITING,
        /** In this status, click and drag to move the view. */
        PANNING
    }

    /**
     * Returns the currently active modifiers.
     * @return the currently active modifiers.
     */
    ImmutableSet<KeyboardModifier> getActiveModifiers();

    /**
     * Sets a certain modifier to an active or inactive state.
     * @param modifier the modifier to be set
     * @param active whether the modifier is active or not
     */
    void setModifier(@NotNull KeyboardModifier modifier, boolean active);

    /**
     * Keys that can modify the behavior of certain interactive functions
     */
    enum KeyboardModifier {
        CTRL
    }

    /**
     * Sets the canvas used for user interaction
     * @param canvas
     */
    void setInteractionCanvas(Canvas canvas);
}
