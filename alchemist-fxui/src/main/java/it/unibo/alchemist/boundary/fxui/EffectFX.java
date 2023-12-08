/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui;

import it.unibo.alchemist.model.Concentration;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position2D;

import java.io.Serializable;
import java.util.Queue;

/**
 * Graphical visualization of something happening in the environment.
 *
 * @param <P> the position type
 */
public interface EffectFX<P extends Position2D<? extends P>> extends Serializable {

    /**
     * Computes a queue of commands to Draw something.
     *
     * @param environment the environment to gather data from
     * @param <T>         the {@link Concentration} type
     * @return the queue of commands that should be run to draw the effect
     */
    <T> Queue<DrawCommand<P>> computeDrawCommands(Environment<T, P> environment);

    /**
     * Gets the name of the effect.
     *
     * @return the name of the effect
     */
    String getName();

    /**
     * Sets the name of the effect.
     *
     * @param name the name of the effect to set
     */
    void setName(String name);

    /**
     * Gets the visibility of the effect.
     *
     * @return the visibility of the effect
     */
    boolean isVisible();

    /**
     * Sets the visibility of the effect.
     *
     * @param visibility the visibility of the effect to set
     */
    void setVisibility(boolean visibility);
}
