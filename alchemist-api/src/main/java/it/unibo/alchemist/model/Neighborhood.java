/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;

import org.danilopianini.util.ListSet;

import java.io.Serializable;

/**
 * A neighborhood, namely the set of nodes to which a "central" node is connected to.
 *
 * @param <T> concentration type
 */
public interface Neighborhood<T> extends Serializable, Cloneable, Iterable<Node<T>> {

    /**
     * @param node
     *            the {@link Node} to add
     * @return a new {@link Neighborhood} with the same center and the new node
     *         among its neighbors
     */
    Neighborhood<T> add(Node<T> node);

    /**
     * Verifies if a node is contained inside a neighborhood.
     *
     * @param n
     *            the node to be searched
     * @return true if n belongs to this neighborhood
     */
    boolean contains(Node<T> n);

    /**
     * Allows accessing the central node.
     *
     * @return the central node, namely the node whose neighbors are represented by
     *         this structure.
     */
    Node<T> getCenter();

    /**
     * Allows directly accessing every node in the neighborhood.
     * A change of this List will be reflected in the neighborhood.
     *
     * @return the {@link java.util.List} of the neighbors
     */
    ListSet<? extends Node<T>> getNeighbors();

    /**
     * @return true if this neighborhood has no neighbors
     */
    boolean isEmpty();

    /**
     * @param node
     *            the {@link Node} to remove
     * @return a new {@link Neighborhood} with the same center without the provided
     *         {@link Node}
     */
    Neighborhood<T> remove(Node<T> node);

    /**
     * @return the number of neighbors.
     */
    int size();

}
