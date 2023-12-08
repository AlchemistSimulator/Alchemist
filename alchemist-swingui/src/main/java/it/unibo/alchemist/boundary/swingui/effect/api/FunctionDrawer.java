/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.api;

import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position2D;

import java.awt.Graphics2D;
import java.util.function.Function;

/**
 * Defines an object capable of drawing functions that take a Position of type P as input and give a
 * Number as output.
 */
public interface FunctionDrawer {

    /**
     * Draw the provided function.
     *
     * @param function        - the function
     * @param environment      - the environment (mainly used to make positions)
     * @param graphics        - the Graphics2D (where to draw the function)
     * @param wormhole - the wormhole (to map env points to view points)
     * @param <T>      - concentration type
     * @param <P>      - position type
     */
    <T, P extends Position2D<P>> void drawFunction(
            Function<? super P, ? extends Number> function,
            Environment<T, P> environment,
            Graphics2D graphics,
            Wormhole2D<P> wormhole
    );

}
