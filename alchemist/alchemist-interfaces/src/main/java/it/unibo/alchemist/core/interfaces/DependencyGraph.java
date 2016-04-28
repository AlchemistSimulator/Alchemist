/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.interfaces;

import it.unibo.alchemist.model.interfaces.Node;

import java.io.Serializable;


/**
 * This interface allows to separate the usage of a dependency graph from its
 * implementation.
 * 
 * @param <T>
 *            The parametrization type for reactions
 */
public interface DependencyGraph<T> extends Serializable {

    /**
     * Given two nodes, the graph assumes they are now neighbors and calculates
     * the neighborhood dependencies between them.
     * 
     * @param n1
     *            The first node
     * @param n2
     *            The second node
     */
    void addNeighbor(Node<T> n1, Node<T> n2);

    /**
     * This method creates the dependencies when a new reaction is added to the
     * environment. Please be careful when building the environment and
     * populating the existing reactions map: this method assumes that all the
     * dependencies among the existing reactions are correct and up to date.
     * 
     * @param rh
     *            the reaction handler whose dependencies should be calculated.
     */
    void createDependencies(DependencyHandler<T> rh);

    /**
     * This method removes all the dependencies (both in and out dependencies)
     * for a given reaction handler. This method is meant to be used in order to
     * keep the dependencies clean when removing a reaction.
     * 
     * @param rh
     *            the reaction handler whose dependencies will be deleted.
     */
    void removeDependencies(DependencyHandler<T> rh);

    /**
     * Given two nodes, the engine assumes they are no longer neighbors and
     * deletes the neighborhood dependencies between them.
     * 
     * @param n1
     *            The first node
     * @param n2
     *            The second node
     */
    void removeNeighbor(Node<T> n1, Node<T> n2);

}
