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
import java.util.Objects;

/**
 * Represent the action of removing a junction between the current node and a neighbor.
 * This action only removes the junction reference inside this node; the neighbor totally ignores
 * that a junction has been removed.
 * This is a part of the junction removal process.
 * See {@link RemoveJunctionInNeighbor} for the other part of the process
 */
public final class RemoveJunctionInCell extends AbstractNeighborAction<Double> { // TODO try local

    @Serial
    private static final long serialVersionUID = 3565077605882164314L;

    private final Junction jun;
    private final Environment<Double, ?> environment;
    private final CellProperty<?> cell;

    /**
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
        cell = Objects.requireNonNull(
                node.asPropertyOrNull(CellProperty.class),
                "This Action can be set only in nodes with " + CellProperty.class.getSimpleName()
        );
        declareDependencyTo(junction);
        for (final Map.Entry<Biomolecule, Double> entry : junction.getMoleculesInCurrentNode().entrySet()) {
            declareDependencyTo(entry.getKey());
        }
        jun = junction;
        this.environment = environment;
    }

    @Override
    public RemoveJunctionInCell cloneAction(final Node<Double> newNode, final Reaction<Double> newReaction) {
        return new RemoveJunctionInCell(environment, newNode, jun, getRandomGenerator());
    }

    /**
     * If no target node is given, DO NOTHING. The junction cannot be removed.
     */
    @Override
    public void execute() { }

    /**
     * Removes the junction that links the node where this action is executed and the target node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(final Node<Double> targetNode) {
        if (targetNode.asPropertyOrNull(CellProperty.class) != null) {
            cell.removeJunction(jun, targetNode);
        } else {
            throw new UnsupportedOperationException(
                "Can't remove Junction in a node with no " + CellProperty.class.getSimpleName()
            );
        }
    }

    @Override
    public String toString() {
        return "remove junction " + jun.toString() + " in cell";
    }
}
