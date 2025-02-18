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
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import it.unibo.alchemist.model.biochemistry.molecules.Junction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serial;

/**
 * Represent the action of add a junction between a neighbor and the current node.
 * This action only creates the junction reference inside the neighbor; the current node totally ignores
 * that a junction has been created.
 * This is a part of the junction creation process.
 * See {@link AddJunctionInCell} for the other part of the process
 *
 * @param <P> Position type
 */
public final class AddJunctionInNeighbor<P extends Position<? extends P>> extends AbstractNeighborAction<Double> {

    @Serial
    private static final long serialVersionUID = 8670229402770243539L;

    private final Junction jun;

    /**
     * @param junction the junction
     * @param node the current node which contains this action. It is NOT the node where the junction will be created.
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    public AddJunctionInNeighbor(
            final Environment<Double, P> environment,
            final Node<Double> node,
            final Junction junction,
            final RandomGenerator randomGenerator
    ) {
        super(node, environment, randomGenerator);
        declareDependencyTo(junction);
        jun = junction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AddJunctionInNeighbor<P> cloneAction(final Node<Double> newNode, final Reaction<Double> newReaction) {
        if (newNode.asPropertyOrNull(CellProperty.class) != null) {
            return new AddJunctionInNeighbor<>(
                    (Environment<Double, P>) getEnvironment(),
                newNode,
                    jun, getRandomGenerator());
        }
        throw new IllegalArgumentException("Node must have a " + CellProperty.class.getSimpleName());
    }

    /**
     * If no target node is given, DO NOTHING. The junction cannot be created.
     *
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
        if (targetNode.asPropertyOrNull(CellProperty.class) != null) {
            targetNode.asProperty(CellProperty.class).addJunction(jun, getNode());
        } else {
            throw new UnsupportedOperationException("Can't add Junction in a node with no "
                    + CellProperty.class.getSimpleName());
        }
    }

    @Override
    public String toString() {
        return "add junction " + jun.toString() + " in neighbor";
    }

}
