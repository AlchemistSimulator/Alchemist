/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.conditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.biochemistry.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.conditions.AbstractCondition;
import it.unibo.alchemist.model.biochemistry.CircularCellProperty;
import it.unibo.alchemist.model.biochemistry.CircularDeformableCellProperty;

import java.util.stream.Stream;

/**
 *
 */
@SuppressFBWarnings("FE_FLOATING_POINT")
public final class TensionPresent extends AbstractCondition<Double> {

    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells<?> environment;

    /**
     *
     * @param node the node
     * @param environment the environment
     */
    public TensionPresent(final EnvironmentSupportingDeformableCells<?> environment, final Node<Double> node) {
        super(node);
        this.environment = environment;
    }

    @Override
    public TensionPresent cloneCondition(final Node<Double> node, final Reaction<Double> reaction) {
        if (node.asPropertyOrNull(CircularDeformableCellProperty.class) != null) {
            return new TensionPresent(environment, node);
        }
        throw new IllegalArgumentException("Node must have a "
                + CircularDeformableCellProperty.class.getSimpleName());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityContribution() {
        final Node<Double> thisNode = getNode();
        return environment.getNodesWithinRange(thisNode, environment.getMaxDiameterAmongCircularDeformableCells()).stream()
                //.parallel()
                .flatMap(node ->
                    node.asPropertyOrNull(CircularCellProperty.class) != null ? Stream.of(node) : Stream.empty()
                )
                .mapToDouble(node -> {
                    final double maxRn;
                    final double minRn;
                    final double maxRN = thisNode.asProperty(CircularDeformableCellProperty.class)
                            .getMaximumRadius();
                    final double minRN = thisNode.asProperty(CircularDeformableCellProperty.class).getRadius();
                    if (node.asPropertyOrNull(CircularDeformableCellProperty.class) != null) {
                        maxRn = node.asProperty(CircularDeformableCellProperty.class).getMaximumRadius();
                        minRn = node.asProperty(CircularDeformableCellProperty.class).getRadius();
                    } else {
                        maxRn = node.asProperty(CircularCellProperty.class).getRadius();
                        minRn = maxRn;
                    }
                    final double distance = environment.getDistanceBetweenNodes(node, thisNode);
                    if (maxRn + maxRN - distance < 0) {
                        return 0;
                    } else {
                        if (maxRn == minRn && maxRN == minRN) {
                            return 1;
                        } else {
                            return (maxRn + maxRN - distance) / (maxRn + maxRN - minRn - minRN);
                        }
                    }
                })
                .sum();
    }

    @Override
    public boolean isValid() {
        final Node<Double> thisNode = getNode();
        return environment.getNodesWithinRange(thisNode, environment.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .flatMap(n -> n.asPropertyOrNull(CircularCellProperty.class) != null
                        ? Stream.of(n)
                        : Stream.empty())
                .anyMatch(n -> {
                    final double maxDN =  thisNode.asProperty(CircularDeformableCellProperty.class)
                            .getMaximumRadius();
                    if (n.asPropertyOrNull(CircularDeformableCellProperty.class) != null) {
                        return environment.getDistanceBetweenNodes(n, thisNode)
                                < (maxDN + n.asProperty(CircularDeformableCellProperty.class)
                                .getMaximumRadius());
                    } else {
                        return environment.getDistanceBetweenNodes(n, thisNode) < (maxDN
                                + n.asProperty(CircularCellProperty.class).getRadius());
                    }
                });
    }

}
