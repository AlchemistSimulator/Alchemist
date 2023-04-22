/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.movestrategies;

import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;

import java.io.Serializable;

/**
 * Given the current target {@link Position}, this strategy interface computes
 * the current {@link Node}'s speed.
 *
 * @param <T> Concentration type
 * @param <P> position type
 */
@FunctionalInterface
public interface SpeedSelectionStrategy<T, P extends Position<? extends P>> extends Serializable {

    /**
     * @param target
     *            the {@link Position} describing where the {@link Node} is
     *            directed
     * @return the current node's movement. The returned value represents a length.
     */
    double getNodeMovementLength(P target);

    /**
     * @param destination the {@link Node} where the strategy is being cloned
     * @param reaction the {@link Reaction} where strategy is being cloned
     *
     * @return A copy of the strategy if the strategy is stateful, and this object otherwise.
     * The default implementation assumes a stateless strategy.
     */
    default SpeedSelectionStrategy<T, P> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return this;
    }
}
