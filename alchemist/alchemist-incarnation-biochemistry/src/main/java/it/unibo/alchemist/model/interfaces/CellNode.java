/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces;

import java.util.Map;
import java.util.Set;

import it.unibo.alchemist.model.implementations.molecules.Junction;

/**
 * @param <P> position type
 */
public interface CellNode<P extends Position<? extends P>> extends Node<Double> {
    /**
     * 
     * @return the map junction - node - quantity
     */
    Map<Junction, Map<CellNode<?>, Integer>> getJunctions();

    /**
     * Add a junction to the current node.
     * @param j the junction
     * @param neighbor the neighbor node at the other side of the junction
     */
    void addJunction(Junction j, CellNode<?> neighbor);

    /**
     * Return true if a junction is present in the current node, false otherwise.
     * Note: a junction is considered present if the method junction.equals(j) return true. 
     * @param j the junction
     * @return true if the junction is present, false otherwise.
     */
    boolean containsJunction(Junction j);

    /**
     * Remove a junction from this node.
     * @param j the junction to remove
     * @param neighbor the node at the other side of the junction.
     */
    void removeJunction(Junction j, CellNode<?> neighbor);

    /**
     * Returns a set of ICellNode which are linked with the current node by a junction of the type j.
     * @param j the junction
     * @return a set of ICellNode which are linked with the current node by a junction of the type j
     */
    Set<CellNode<?>> getNeighborsLinkWithJunction(Junction j);

    /**
     * 
     * @return The total number of junctions presents in this node
     */
    int getJunctionsCount();

    /**
     * 
     * @return A set of nodes which are linked by a junction with the current node
     */
    Set<CellNode<?>> getAllNodesLinkWithJunction();

    /**
     * set the polarization versor, e.g. a versor indicating the direction in which the cell will move the next time.
     * @param v The {@link Position} representing the new polarization versor. Note: v MUST be a versor, so a vector with module = 1.
     */
    void setPolarization(P v);

    /**
     * 
     * @return the {@link Position} representing the direction of cell polarization.
     */
    P getPolarizationVersor();

    /**
     * add v to the polarization versor inside the cell; useful for considering the combination of various stimuli in a cell.
     * @param v polarization direction
     */
    void addPolarization(P v);

}
