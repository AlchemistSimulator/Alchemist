/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.implementation.adapter.ComponentViewPort;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;

import java.awt.Component;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * An implementation of [AbstractWormhole2D] for Swing.
 *
 * @param <P> the type of the position
 */
public class WormholeSwing<P extends Position2D<? extends P>> extends AbstractWormhole2D<P> {

    /**
     * @param environment the environment
     * @param component the {@link it.unibo.alchemist.boundary.wormhole.interfaces.ViewPort}
     *   of the UI used for implementing the wormhole
     */
    public WormholeSwing(final Environment<?, P> environment, final Component component) {
        super(
            environment,
            new ComponentViewPort(component),
            viewType -> from(viewType.getWidth() / 2.0, viewType.getHeight() / 2.0)
        );
    }
}
