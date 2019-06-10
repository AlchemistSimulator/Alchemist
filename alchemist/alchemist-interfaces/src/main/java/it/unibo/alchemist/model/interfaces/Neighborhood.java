/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

import org.danilopianini.util.ListSet;

/**
 * The type which describes the concentration of a molecule
 * 
 * Interface for a neighborhood. When implementing it in a real class, please
 * remember to correctly implement also the equals method inherited from Object.
 * 
 * @param <T>
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
     * Allows to access the central node.
     * 
     * @return the central node, namely the node whose neighbors are represented by
     *         this structure.
     */
    Node<T> getCenter();

    /**
     * Returns the num-th neighbor.
     * 
     * @param num
     *            the neighbor index
     * @return the num-th neighbor
     */
    @Deprecated
    Node<T> getNeighborByNumber(int num);

    /**
     * Allows to directly access every node in the neighborhood. A change of this
     * List will be reflected in the neighborhood.
     * 
     * @return the list of the neighbors
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
