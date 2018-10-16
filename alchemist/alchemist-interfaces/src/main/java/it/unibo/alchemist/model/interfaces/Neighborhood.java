/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
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
     *            the {@link Node} to plus
     * @return a new {@link Neighborhood} with the same center and the new node
     *         among its neighbors
     */
    Neighborhood<T> add(Node<T> node);

    /**
     * Verifies if a node with the specified id is contained inside a neighborhood.
     * 
     * @param n
     *            the node id to be searched
     * @return true if n belongs to this neighborhood
     */
    boolean contains(int n);

    /**
     * Verifies if a node is contained inside a neighborhood.
     * 
     * @param n
     *            the node to be searched
     * @return true if n belongs to this neighborhood
     */
    boolean contains(Node<T> n);

    /**
     * Allows to get all the nodes in this neighborhood whose distance from the
     * center node is between min and max.
     * 
     * @param min
     *            if a node has a distance from the center node lower than min, it
     *            will be not in the returned list
     * @param max
     *            if a node has a distance from the center node higher than max, it
     *            will be not in the returned list
     * @return the list of nodes whose distance from the center node is between min
     *         and max.
     */
    ListSet<? extends Node<T>> getBetweenRange(double min, double max);

    /**
     * Allows to access the central node.
     * 
     * @return the central node, namely the node whose neighbors are represented by
     *         this structure.
     */
    Node<T> getCenter();

    /**
     * @param id
     *            the node id
     * @return the nodes
     */
    Node<T> getNeighborById(int id);

    /**
     * Returns the num-th neighbor.
     * 
     * @param num
     *            the neighbor index
     * @return the num-th neighbor
     */
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
