/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;
import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * Represent the action of add a junction between the current node and a neighbor.
 * This action only create the junction reference inside this node, the neighbor totally ignore 
 * that a junction has been created.
 * This is a part of the junction creation process.
 * See {@link AddJunctionInNeighbor} for the other part of the process
 */
public final class AddJunctionInCell extends AbstractNeighborAction<Double> { // TODO try with local

    private static final long serialVersionUID = -7074995950043793067L;

    private final Junction jun;

    /**
     * @param j the junction
     * @param n the current node
     * @param e the current environment
     * @param rg the random generator
     */
    public AddJunctionInCell(final Environment<Double, ?> e, final Node<Double> n, final Junction j, final RandomGenerator rg) {
        super(n, e, rg);
        if (n.asPropertyOrNull(CellProperty.class) != null) {
            declareDependencyTo(j);
            jun = j;
        } else {
            throw new UnsupportedOperationException("This Action can be set only in nodes with "
                    + CellProperty.class.getSimpleName());
        }
    }

    @Override
    public AddJunctionInCell cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new AddJunctionInCell(getEnvironment(), node, jun, getRandomGenerator());
    }

    /**
     * If no target node is given DO NOTHING. The junction can not be created.
     * @throws UnsupportedOperationException if this method is called.
     */
    @Override
    public void execute() {
        throw new UnsupportedOperationException("A junction CAN NOT be created without a target node.");
    }

    /**
     * Create the junction that links the node where this action is executed and the target node. 
     */
    @Override
    public void execute(final Node<Double> targetNode) { 
        if (targetNode.asPropertyOrNull(CellProperty.class) != null) {
            getNode().asProperty(CellProperty.class).addJunction(jun, targetNode);
        } else {
            throw new UnsupportedOperationException("Can't add Junction in a node with no "
                    + CellProperty.class.getSimpleName());
        }
    }

    @Override 
    public String toString() {
        return "add junction " + jun.toString() + " in node";
    }
}
