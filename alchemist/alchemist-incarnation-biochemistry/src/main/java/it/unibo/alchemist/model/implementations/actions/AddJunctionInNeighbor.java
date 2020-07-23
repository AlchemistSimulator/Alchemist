/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represent the action of add a junction between a neighbor and the current node. <br/>
 * This action only create the junction reference inside the neighbor, the current node totally ignore 
 * that a junction has been created.  <br/>
 * This is a part of the junction creation process. <br/>
 * See {@link AddJunctionInCell} for the other part of the process
 * @param <P> Position type
 */
public final class AddJunctionInNeighbor<P extends Position<? extends P>> extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = 8670229402770243539L;

    private final Junction jun;
    /**
     * 
     * @param junction the junction
     * @param node the current node which contains this action. It is NOT the node where the junction will be created.
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    public AddJunctionInNeighbor(
            final Environment<Double, P> environment,
            final CellNode<P> node,
            final Junction junction,
            final RandomGenerator randomGenerator
    ) {
        super(node, environment, randomGenerator);
        declareDependencyTo(junction);
        jun = junction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AddJunctionInNeighbor<P> cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        if (node instanceof CellNode) {
            return new AddJunctionInNeighbor<>(
                    (Environment<Double, P>) getEnvironment(),
                    (CellNode<P>) node,
                    jun, getRandomGenerator());
        }
        throw new IllegalArgumentException("Node must be CellNode, found " + node + " of type: " + node.getClass());
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
     * Create the junction that links the target node and the node when this action is executed. 
     */
    @Override
    @SuppressWarnings("unchecked")
    public void execute(final Node<Double> targetNode) {
        if (targetNode instanceof CellNode) {
            ((CellNode<P>) targetNode).addJunction(jun, getNode());
        } else {
            throw new UnsupportedOperationException("Can't add Junction in a node that it's not a CellNode");
        }
    }

    @Override 
    public String toString() {
        return "add junction " + jun.toString() + " in neighbor";
    }

    @Override
    @SuppressWarnings("unchecked")
    public CellNode<P> getNode() {
        return (CellNode<P>) super.getNode();
    }

}
