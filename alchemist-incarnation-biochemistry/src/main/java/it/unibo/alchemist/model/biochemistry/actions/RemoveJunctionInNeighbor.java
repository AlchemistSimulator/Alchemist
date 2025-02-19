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
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.biochemistry.molecules.Junction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serial;
import java.util.Map;

/**
 * Represent the action of removing a junction between a neighbor and the current node.
 * This action only removes the junction reference inside the neighbor node; the current one totally ignores
 * that a junction has been removed.
 * This is a part of the junction removal process.
 * See {@link RemoveJunctionInCell} for the other part of the process.
 */
public final class RemoveJunctionInNeighbor extends AbstractNeighborAction<Double> {

    @Serial
    private static final long serialVersionUID = -5033532863301442377L;

    private final Junction jun;

    /**
     * @param junction junction to remove
     * @param node the node
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    @SuppressWarnings("unchecked")
    public RemoveJunctionInNeighbor(
            final Environment<Double, ?> environment,
            final Node<Double> node,
            final Junction junction,
            final RandomGenerator randomGenerator) {
        super(node, environment, randomGenerator);
        if (node.asPropertyOrNull(CellProperty.class) != null) {
            declareDependencyTo(junction);
            for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
                declareDependencyTo(entry.getKey());
            }
            jun = junction;
        } else {
            throw new UnsupportedOperationException(
                    "This Action can be set only in nodes with " + CellProperty.class.getSimpleName()
            );
        }
    }

    @Override
    public RemoveJunctionInNeighbor cloneAction(final Node<Double> newNode, final Reaction<Double> newReaction) {
        return new RemoveJunctionInNeighbor(getEnvironment(), newNode, jun, getRandomGenerator());
    }

    /**
     * If no target node is given, DO NOTHING. The junction cannot be removed.
     */
    @Override
    public void execute() { }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final Node<Double> targetNode) {
        if (targetNode.asPropertyOrNull(CellProperty.class) != null) {
            targetNode.asProperty(CellProperty.class).removeJunction(jun, getNode());
        } else {
            throw new UnsupportedOperationException("Can't add Junction in a node with no "
                    + CellProperty.class.getSimpleName());
        }
    }

    @Override
    public String toString() {
        return "remove junction " + jun.toString() + " in neighbor";
    }
}
