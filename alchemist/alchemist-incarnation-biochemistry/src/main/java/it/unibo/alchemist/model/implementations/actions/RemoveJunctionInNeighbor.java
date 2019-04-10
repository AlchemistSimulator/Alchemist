/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represent the action of removing a junction between a neighbor and the current node. <br/>
 * This action only remove the junction reference inside the neighbor node, the current one totally ignore 
 * that a junction has been removed. <br/>
 * This is a part of the junction removal process. <br/>
 * See {@link RemoveJunctionInCell} for the other part of the process.
 */
public final class RemoveJunctionInNeighbor extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = -5033532863301442377L;

    private final Junction jun;

    /**
     * 
     * @param junction junction to remove
     * @param node the node
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    public RemoveJunctionInNeighbor(
            final Environment<Double, ?> environment,
            final Node<Double> node,
            final Junction junction,
            final RandomGenerator randomGenerator) {
        super(node, environment, randomGenerator);
        if (node instanceof CellNode) {
            declareDependencyTo(junction);
            for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
                declareDependencyTo(entry.getKey());
            }
            jun = junction;
        } else {
            throw new UnsupportedOperationException("This Action can be set only in CellNodes");
        }
    }

    @Override
    public RemoveJunctionInNeighbor cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new RemoveJunctionInNeighbor(getEnvironment(), n, jun, getRandomGenerator());
    }

    /**
     * If no target node is given DO NOTHING. The junction can not be removed.
     */
    @Override
    public void execute() { }

    @Override
    public void execute(final Node<Double> targetNode) {
        if (targetNode instanceof CellNode) {
            ((CellNode<?>) targetNode).removeJunction(jun, getNode());
        } else {
            throw new UnsupportedOperationException("Can't add Junction in a node that it's not a CellNode");
        }
    }

    @Override 
    public String toString() {
        return "remove junction " + jun.toString() + " in neighbor";
    }

    @Override
    public CellNode<?> getNode() {
        return (CellNode<?>) super.getNode();
    }

}
