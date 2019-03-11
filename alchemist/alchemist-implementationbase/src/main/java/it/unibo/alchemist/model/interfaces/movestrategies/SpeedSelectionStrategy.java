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

import java.io.Serializable;

/**
 * Given the current target {@link Position}, this strategy interface computes
 * the current {@link Node}'s speed.
 * 
 */
@FunctionalInterface
public interface SpeedSelectionStrategy<P extends Position<? extends P>> extends Serializable {

    /**
     * @param target
     *            the {@link Position} describing where the {@link Node} is
     *            directed
     * @return the current node's movement. The returned value represents a length.
     */
    double getNodeMovementLength(P target);

}
