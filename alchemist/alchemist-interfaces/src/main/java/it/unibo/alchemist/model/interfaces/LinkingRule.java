/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 * An interface that represent an auto-linking logic for nodes within an
 * environment.
 * 
 * @param <T>
 *            The type which describes the concentration of a molecule
 */
public interface LinkingRule<T> extends Serializable {

    /**
     * Produces a new neighborhood for specified node considering its position.
     * 
     * @param center
     *            the node to recompute
     * @param env
     *            the node's environment
     * @return a neighborhood
     */
    Neighborhood<T> computeNeighborhood(Node<T> center, Environment<T> env);

}
