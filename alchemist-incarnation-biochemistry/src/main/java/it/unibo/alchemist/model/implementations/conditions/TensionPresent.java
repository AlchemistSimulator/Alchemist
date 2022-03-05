/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CircularCellularProperty;
import it.unibo.alchemist.model.interfaces.properties.CircularDeformableCellularProperty;

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
        if (node.asPropertyOrNull(CircularDeformableCellularProperty.class) != null) {
            return new TensionPresent(environment, node);
        }
        throw new IllegalArgumentException("Node must have a "
                + CircularDeformableCellularProperty.class.getSimpleName());
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
                .flatMap(n -> n.asPropertyOrNull(CircularCellularProperty.class) != null
                        ? Stream.of(n)
                        : Stream.empty())
                .mapToDouble(n -> {
                    final double maxRn;
                    final double minRn;
                    final double maxRN = thisNode.asProperty(CircularDeformableCellularProperty.class)
                            .getMaximumRadius();
                    final double minRN = thisNode.asProperty(CircularDeformableCellularProperty.class).getRadius();
                    if (n.asPropertyOrNull(CircularDeformableCellularProperty.class) != null) {
                        final Node<Double> cell = n;
                        maxRn = cell.asProperty(CircularDeformableCellularProperty.class).getMaximumRadius();
                        minRn = cell.asProperty(CircularDeformableCellularProperty.class).getRadius();
                    } else {
                        maxRn = n.asProperty(CircularCellularProperty.class).getRadius();
                        minRn = maxRn;
                    }
                    final double distance = environment.getDistanceBetweenNodes(n, thisNode);
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
                .flatMap(n -> n.asPropertyOrNull(CircularCellularProperty.class) != null
                        ? Stream.of(n)
                        : Stream.empty())
                .anyMatch(n -> {
                    final double maxDN =  thisNode.asProperty(CircularDeformableCellularProperty.class)
                            .getMaximumRadius();
                    if (n.asPropertyOrNull(CircularDeformableCellularProperty.class) != null) {
                        return environment.getDistanceBetweenNodes(n, thisNode)
                                < (maxDN + n.asProperty(CircularDeformableCellularProperty.class)
                                .getMaximumRadius());
                    } else {
                        return environment.getDistanceBetweenNodes(n, thisNode) < (maxDN
                                + n.asProperty(CircularCellularProperty.class).getRadius());
                    }
                });
    }

}
