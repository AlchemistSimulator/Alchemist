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

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * This interface models a strategy for selecting positions where to move.
 *
 * @param <T> Concentration type
 * @param <P> Position type
 */
@FunctionalInterface
public interface TargetSelectionStrategy<T, P extends Position<? extends P>> extends Serializable {

    /**
     * @return the next target where the {@link it.unibo.alchemist.model.interfaces.Node} is directed
     */
    P getTarget();

    /**
     * @return A copy of the strategy if the strategy is stateful, and this object otherwise.
     * The default implementation assumes a stateless strategy.
     */
    default TargetSelectionStrategy<T, P> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return this;
    }
}
