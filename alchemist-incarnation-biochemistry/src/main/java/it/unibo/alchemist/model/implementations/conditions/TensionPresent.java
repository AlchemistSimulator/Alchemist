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
import it.unibo.alchemist.model.interfaces.capabilities.CircularCellularBehavior;
import it.unibo.alchemist.model.interfaces.capabilities.CircularDeformableCellularBehavior;

import java.util.stream.Stream;

/**
 *
 */
@SuppressFBWarnings("FE_FLOATING_POINT")
public final class TensionPresent extends AbstractCondition<Double> {

    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells<?> env;

    /**
     *
     * @param node the node
     * @param env the environment
     */
    public TensionPresent(final EnvironmentSupportingDeformableCells<?> env, final Node<Double> node) {
        super(node);
        this.env = env;
    }

    @Override
    public TensionPresent cloneCondition(final Node<Double> node, final Reaction<Double> reaction) {
        if (node.asCapabilityOrNull(CircularDeformableCellularBehavior.class) != null) {
            return new TensionPresent(env, node);
        }
        throw new IllegalArgumentException("Node must be CircularDeformableCell, found " + node
                + " of type: " + node.getClass());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityContribution() {
        final Node<Double> thisNode = getNode();
        return env.getNodesWithinRange(thisNode, env.getMaxDiameterAmongCircularDeformableCells()).stream()
                //.parallel()
                .flatMap(n -> n.asCapabilityOrNull(CircularCellularBehavior.class) != null
                        ? Stream.of(n)
                        : Stream.empty())
                .mapToDouble(n -> {
                    final double maxRn;
                    final double minRn;
                    final double maxRN = thisNode.asCapability(CircularDeformableCellularBehavior.class)
                            .getMaximumRadius();
                    final double minRN = thisNode.asCapability(CircularDeformableCellularBehavior.class).getRadius();
                    if (n.asCapabilityOrNull(CircularDeformableCellularBehavior.class) != null) {
                        final Node<Double> cell = n;
                        maxRn = cell.asCapability(CircularDeformableCellularBehavior.class).getMaximumRadius();
                        minRn = cell.asCapability(CircularDeformableCellularBehavior.class).getRadius();
                    } else {
                        maxRn = n.asCapability(CircularCellularBehavior.class).getRadius();
                        minRn = maxRn;
                    }
                    final double distance = env.getDistanceBetweenNodes(n, thisNode);
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
        return env.getNodesWithinRange(thisNode, env.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .flatMap(n -> n.asCapabilityOrNull(CircularCellularBehavior.class) != null
                        ? Stream.of(n)
                        : Stream.empty())
                .anyMatch(n -> {
                    final double maxDN =  thisNode.asCapability(CircularDeformableCellularBehavior.class)
                            .getMaximumRadius();
                    if (n.asCapabilityOrNull(CircularDeformableCellularBehavior.class) != null) {
                        return env.getDistanceBetweenNodes(n, thisNode)
                                < (maxDN + n.asCapability(CircularDeformableCellularBehavior.class)
                                .getMaximumRadius());
                    } else {
                        return env.getDistanceBetweenNodes(n, thisNode) < (maxDN
                                + n.asCapability(CircularCellularBehavior.class).getRadius());
                    }
                });
    }

}
