/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * 
 */
public class TensionPresent extends AbstractCondition<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells<?> env;

    /**
     * 
     * @param node 
     * @param env 
     */
    public TensionPresent(final EnvironmentSupportingDeformableCells<?> env, final CircularDeformableCell<?> node) {
        super(node);
        this.env = env;
    }

    @Override
    public TensionPresent cloneCondition(final Node<Double> n, final Reaction<Double> r) {
        return new TensionPresent(env, (CircularDeformableCell<?>) n);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityContribution() {
        final CircularDeformableCell<?> thisNode = (CircularDeformableCell<?>) getNode();
        return env.getNodesWithinRange(thisNode, env.getMaxDiameterAmongCircularDeformableCells()).stream()
                //.parallel()
                .flatMap(n -> n instanceof CellWithCircularArea 
                        ? Stream.of((CellWithCircularArea<?>) n) 
                                : Stream.empty())
                .mapToDouble(n -> {
                    final double maxRn;
                    final double minRn;
                    final double maxRN = thisNode.getMaxRadius();
                    final double minRN = thisNode.getRadius();
                    if (n instanceof CircularDeformableCell) {
                        final CircularDeformableCell<?> cell = (CircularDeformableCell<?>) n;
                        maxRn = cell.getMaxRadius();
                        minRn = cell.getRadius();
                    } else {
                        maxRn = n.getRadius();
                        minRn = maxRn;
                    }
                    final double distance = env.getDistanceBetweenNodes(n, thisNode);
                    if (((maxRn + maxRN) - distance) < 0) {
                        return 0;
                    } else {
                        if (maxRn == minRn && maxRN == minRN) {
                            return 1;
                        } else {
                            return ((maxRn + maxRN) - distance) / ((maxRn + maxRN) - (minRn + minRN));
                        }
                    }
                })
                .sum();
    }

    @Override
    public boolean isValid() {
        final CircularDeformableCell<?> thisNode = (CircularDeformableCell<?>) getNode();
        return env.getNodesWithinRange(thisNode, env.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .flatMap(n -> n instanceof CellWithCircularArea 
                        ? Stream.of((CellWithCircularArea<?>) n) 
                                : Stream.empty())
                .filter(n -> {
                    final double maxDN =  thisNode.getMaxRadius();
                    if (n instanceof CircularDeformableCell) {
                        return env.getDistanceBetweenNodes(n, thisNode) < (maxDN + ((CircularDeformableCell<?>) n).getMaxRadius());
                    } else {
                        return env.getDistanceBetweenNodes(n, thisNode) < (maxDN + n.getRadius());
                    }
                })
                .findAny()
                .isPresent();
    }

}
