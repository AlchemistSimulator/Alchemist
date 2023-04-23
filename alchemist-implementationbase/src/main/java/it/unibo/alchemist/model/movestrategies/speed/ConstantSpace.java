/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.movestrategies.speed;

import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy;

/**
 * This strategy makes the node move every time of a fixed amount of space.
 *
 * @param <T> Concentration type
 * @param <P> Position type
 *
 */
public final class ConstantSpace<T, P extends Position<P>> implements SpeedSelectionStrategy<T, P> {

    private static final long serialVersionUID = 1L;
    private final double space;

    /**
     * @param step
     *            the step length (in meters, or the unit you are using in your simulation)
     */
    public ConstantSpace(final double step) {
        this.space = step;
    }

    @Override
    public double getNodeMovementLength(final P target) {
        return space;
    }

}
