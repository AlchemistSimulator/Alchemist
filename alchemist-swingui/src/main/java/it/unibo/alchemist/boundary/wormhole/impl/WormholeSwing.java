/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.impl;

import it.unibo.alchemist.boundary.ui.impl.AbstractWormhole2D;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position2D;

import java.awt.Component;

import static it.unibo.alchemist.boundary.ui.impl.PointAdapter.from;

/**
 * An implementation of [AbstractWormhole2D] for Swing.
 *
 * @param <P> the type of the position
 */
public class WormholeSwing<P extends Position2D<? extends P>> extends AbstractWormhole2D<P> {

    /**
     * @param environment the environment
     * @param component the {@link it.unibo.alchemist.boundary.ui.api.ViewPort}
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
