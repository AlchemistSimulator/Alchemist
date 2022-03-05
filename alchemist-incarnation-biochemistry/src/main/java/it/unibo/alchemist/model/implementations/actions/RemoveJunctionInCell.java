/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellularProperty;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

/**
 * Represent the action of removing a junction between the current node and a neighbor.
 * This action only remove the junction reference inside this node, the neighbor totally ignore 
 * that a junction has been removed.
 * This is a part of the junction removal process.
 * See {@link RemoveJunctionInNeighbor} for the other part of the process
 */
public final class RemoveJunctionInCell extends AbstractNeighborAction<Double> { // TODO try local

    private static final long serialVersionUID = 3565077605882164314L;

    private final Junction jun;
    private final Environment<Double, ?> environment;

    /**
     * 
     * @param junction the junction
     * @param node the node where the action is performed
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    public RemoveJunctionInCell(
            final Environment<Double, ?> environment,
            final Node<Double> node,
            final Junction junction,
            final RandomGenerator randomGenerator
    ) {
        super(node, environment, randomGenerator);
        if (node.asPropertyOrNull(CellularProperty.class) != null) {
            declareDependencyTo(junction);
            for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
                declareDependencyTo(entry.getKey());
            }
            jun = junction;
            this.environment = environment;
        } else {
            throw new UnsupportedOperationException(
                    "This Action can be set only in nodes with " + CellularProperty.class.getSimpleName()
            );
        }
    }

    @Override
    public RemoveJunctionInCell cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new RemoveJunctionInCell(environment, node, jun, getRandomGenerator());
    }

    /**
     * If no target node is given DO NOTHING. The junction can not be removed.
     */
    @Override
    public void execute() { }

    /**
     * Removes the junction that links the node where this action is executed and the target node. 
     */
    @Override
    public void execute(final Node<Double> targetNode) { 
        if (targetNode.asPropertyOrNull(CellularProperty.class) != null) {
            getNode().asProperty(CellularProperty.class).removeJunction(jun, targetNode);
        } else {
            throw new UnsupportedOperationException("Can't remove Junction in a node with no "
                    + CellularProperty.class.getSimpleName());
        }
    }

    @Override 
    public String toString() {
        return "remove junction " + jun.toString() + " in cell";
    }

    @Override
    public Node<Double> getNode() {
        return super.getNode();
    }
}
