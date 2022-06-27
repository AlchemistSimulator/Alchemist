/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 * An interface that represent an auto-linking logic for nodes within an
 * environment.
 * 
 * @param <T>
 *            The type which describes the concentration of a molecule
 * @param <P>
 *            The position type
 */
public interface LinkingRule<T, P extends Position<? extends P>> extends Serializable {

    /**
     * Produces a new neighborhood for specified node considering its position.
     * 
     * @param center
     *            the node to recompute
     * @param environment
     *            the node's environment
     * @return a neighborhood
     */
    Neighborhood<T> computeNeighborhood(Node<T> center, Environment<T, P> environment);

    /**
     * Some rules may require to be evaluated against multiple nodes until the
     * situations gets consistent. For instance, a rule that connects the
     * closest 10 nodes must be evaluated multiple times to get to the correct
     * result (this is because a change in one neighbor may require a
     * disconnection from another node to maintain exactly 10 connections).
     * 
     * Most rules do not need such machinery (e.g., connecting to nodes within
     * some statically defined range).
     * 
     * @return true if this rule does not need to be recursively re-applied to
     *         neighbors to ensure global consistency.
     */
    boolean isLocallyConsistent();

}
