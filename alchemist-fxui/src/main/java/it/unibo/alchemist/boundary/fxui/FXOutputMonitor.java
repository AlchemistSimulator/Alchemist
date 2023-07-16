/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui;

import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.KeyboardActionListener;
import it.unibo.alchemist.model.Position2D;
import javafx.scene.Node;

import java.util.Collection;

/**
 * {@code OutputMonitor} that handles the graphical part of the simulation in JavaFX.
 *
 * @param <T> the {@link it.unibo.alchemist.model.Concentration} type
 * @param <P> the position type
 */
public interface FXOutputMonitor<T, P extends Position2D<? extends P>> extends OutputMonitor<T, P> {

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
     * Repaints this {@link javafx.scene.canvas.Canvas}' {@link javafx.scene.canvas.GraphicsContext}
     * by drawing all the {@link EffectFX Effect}s of each
     * {@link it.unibo.alchemist.model.Node} of the specified
     * {@link it.unibo.alchemist.model.Environment}.
     */
    void repaint();

    /**
     * Getter method for the {@link EffectFX Effects} to draw.
     *
     * @return the current {@code Effects} to draw
     */
    Collection<EffectGroup<P>> getEffects();

    /**
     * Setter method for the effects to draw.
     * <p>
     * All previous set {@link EffectFX Effects} are removed.
     *
     * @param effects the {@code Effects} to draw
     */
    void setEffects(Collection<EffectGroup<P>> effects);

    /**
     * Add all the {@link EffectGroup}s in the collection to the
     * {@link EffectFX Effects} to draw.
     *
     * @param effects the {@link EffectGroup}s to draw
     * @see Collection#addAll(Collection)
     */
    void addEffects(Collection<EffectGroup<P>> effects);

    /**
     * Add the {@link EffectGroup} in the collection to the
     * {@link EffectFX Effects} to draw.
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

    /**
     * Returns the JavaFX Node that is this monitor.
     * @return the node
     */
    Node asJavaFXNode();
}
