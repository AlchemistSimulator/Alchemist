/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import it.unibo.alchemist.model.biochemistry.molecules.Junction;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represent the action of add a junction between the current node and a neighbor.
 * This action only create the junction reference inside this node, the neighbor totally ignore 
 * that a junction has been created.
 * This is a part of the junction creation process.
 * See {@link AddJunctionInNeighbor} for the other part of the process
 */
public final class AddJunctionInCell extends AbstractNeighborAction<Double> { // TODO try with local

    private static final long serialVersionUID = -7074995950043793067L;

    private final Junction junction;

    /**
     * @param junction the junction
     * @param node the current node
     * @param environment the current environment
     * @param randomGenerator the random generator
     */
    @SuppressWarnings("unchecked")
    public AddJunctionInCell(
        final Environment<Double, ?> environment,
        final Node<Double> node,
        final Junction junction,
        final RandomGenerator randomGenerator
    ) {
        super(node, environment, randomGenerator);
        node.asProperty(CellProperty.class);
        declareDependencyTo(junction);
        this.junction = junction;
    }

    @Override
    public AddJunctionInCell cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new AddJunctionInCell(getEnvironment(), node, junction, getRandomGenerator());
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
    @SuppressWarnings("unchecked")
    @Override
    public void execute(final Node<Double> targetNode) { 
        if (targetNode.asPropertyOrNull(CellProperty.class) != null) {
            getNode().asProperty(CellProperty.class).addJunction(junction, targetNode);
        } else {
            throw new UnsupportedOperationException("Can't add Junction in a node with no "
                    + CellProperty.class.getSimpleName());
        }
    }

    @Override 
    public String toString() {
        return "add junction " + junction.toString() + " in node";
    }
}
