/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;

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
     * @param f        - the function
     * @param env      - the environment (mainly used to make positions)
     * @param g        - the Graphics2D (where to draw the function)
     * @param wormhole - the wormhole (to map env points to view points)
     * @param <T>      - concentration type
     * @param <P>      - position type
     */
    <T, P extends Position2D<P>> void drawFunction(Function<? super P, ? extends Number> f, Environment<T, P> env, Graphics2D g, IWormhole2D<P> wormhole);

}
