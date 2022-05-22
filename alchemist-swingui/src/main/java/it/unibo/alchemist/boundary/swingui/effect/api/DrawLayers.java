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
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position2D;

import java.awt.Graphics2D;
import java.util.Collection;

/**
 * Basic interface for every effect that draws something related to {@link it.unibo.alchemist.model.interfaces.Layer}s.
 *
 * This class is a workaround: the {@link Effect} abstraction is meant to add effects
 * to nodes, not to draw layers. At present, is the finest workaround available.
 * This workaround has the following disadvantages:
 * - when there aren't nodes visible in the gui the effects are not used at all,
 * so this effect won't work.
 */
public interface DrawLayers extends Effect {

    /**
     * Effectively draw the layers.
     *
     * @param toDraw   - the layers to draw
     * @param environment      - the environment (mainly used to create positions)
     * @param graphics        - the graphics2D
     * @param wormhole - the wormhole
     * @param <T>      - node concentration type
     * @param <P>      - position type
     */
    <T, P extends Position2D<P>> void drawLayers(
            Collection<Layer<T, P>> toDraw,
            Environment<T, P> environment,
            Graphics2D graphics,
            Wormhole2D<P> wormhole
    );

}
