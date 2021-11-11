/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.interfaces.movestrategies;

import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.io.Serializable;

/**
 * Given the current target {@link Position}, this strategy interface computes
 * the current {@link it.unibo.alchemist.model.interfaces.Node}'s speed.
 *
 * @param <T> Concentration type
 * @param <P> position type
 */
@FunctionalInterface
public interface SpeedSelectionStrategy<T, P extends Position<? extends P>> extends Serializable {

    /**
     * @param target
     *            the {@link Position} describing where the {@link it.unibo.alchemist.model.interfaces.Node} is
     *            directed
     * @return the current node's movement. The returned value represents a length.
     */
    double getNodeMovementLength(P target);

    /**
     * @return A copy of the strategy if the strategy is stateful, and this object otherwise.
     * The default implementation assumes a stateless strategy.
     */
    default SpeedSelectionStrategy<T, P> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return this;
    }
}
