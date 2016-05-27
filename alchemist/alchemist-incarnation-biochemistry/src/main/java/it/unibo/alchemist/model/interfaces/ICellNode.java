/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.util.List;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.implementations.nodes.CellNode;

/**
 */
public interface ICellNode extends Node<Double> {
    /**
     * 
     * @return the list of junctions contained in this node.
     */
    List<Junction> getJunctions();

    /**
     * Add a junction to the current node.
     * @param j the junction
     */
    void addJunction(final Junction j);

    /**
     * Return true if a junction is present in the current node, false otherwise.
     * Note: a junction is considered present if the method junction.equals(j) return true. 
     * The neighbor node should NOT be considered in this comparison, it depends on the implementation of junction.
     * See {@link Junction#equals(Object)} for more details. 
     * 
     * @param j the junction
     * @return true if the junction is present, false otherwise.
     */
    boolean containsJunction(final Junction j);

    /**
     * Remove a junction from this node.
     * @param j the junction to remove
     * @param neighbor the node at the other side of the junction.
     */
    void removeJunction(final Junction j, final CellNode neighbor);
}
