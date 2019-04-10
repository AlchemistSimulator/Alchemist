/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.util.stream.SmallestN;
import org.jooq.lambda.tuple.Tuple2;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.primitives.Doubles;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Non local-consistent rule that connect the closest N nodes together.
 * Two nodes get connected if either one belongs to the set of the ten devices closest to the other.
 * 
 * @param <T>
 * @param <P>
 */
public class ClosestN<T, P extends Position<P>> implements LinkingRule<T, P> {

    private static final long serialVersionUID = 1L;
    private static final double CONNECTION_RANGE_TOLERANCE = 1.1;
    private final Cache<Node<T>, Double> ranges;
    private final int n, expectedNodes;

    /**
     * @param n
     *            neighbors
     * @param expectedNodes
     *            how many nodes are expected to be inserted in the environment
     *            (used for optimization)
     * @param maxNodes
     *            the maximum number of nodes for which the connection range
     *            will be cached
     */
    public ClosestN(final int n, final int expectedNodes, final int maxNodes) {
        if (n < 1) {
            throw new IllegalArgumentException("The parameter must be an integer greater than 0");
        }
        ranges = CacheBuilder.newBuilder()
                .maximumSize(maxNodes)
                .build();
        this.n = n;
        this.expectedNodes = expectedNodes;
    }

    /**
     * @param n
     *            neighbors
     * @param expectedNodes
     *            how many nodes are expected to be inserted in the environment
     *            (used for optimization)
     */
    public ClosestN(final int n, final int expectedNodes) {
        this(n, expectedNodes, expectedNodes);
    }

    /**
     * @param n
     *            neighbors
     */
    public ClosestN(final int n) {
        this(n, 0);
    }

    @Override
    public final Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> env) {
        if (env.getNodesNumber() < expectedNodes || !nodeIsEnabled(center)) {
            return Neighborhoods.make(env, center);
        }
        return Neighborhoods.make(env, center,
                Stream.concat(
                    closestN(center, env),
                    /*
                     * Of all nodes but myself...
                     */
                    env.getNodes().parallelStream()
                        /*
                         * ...select those for which I'm on the closest n
                         */
                        .filter(node -> 
                                !center.equals(node)
                                && closestN(node, env).anyMatch(center::equals)
                        )
                )
                .sequential()
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private Stream<Node<T>> closestN(final Node<T> center, final Environment<T, ?> env) {
        if (!nodeIsEnabled(center)) {
            return Stream.empty();
        }
        double currentRange = getRange(env, center);
        Set<Node<T>> inRange;
        final double maxRange = Doubles.max(env.getSizeInDistanceUnits()) * 2;
        do {
            inRange = (env.getNodesNumber() > n && currentRange < maxRange
                    ? nodesInRange(env, center, currentRange).stream()
                    : env.getNodes().stream())
                        .filter(n -> !n.equals(center) && nodeIsEnabled(n))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            currentRange *= 2;
        } while (inRange.size() < n && inRange.size() < env.getNodesNumber() - 1 && currentRange < maxRange * 2);
        if (inRange.isEmpty()) {
            return Stream.empty();
        }
        final MinMaxPriorityQueue<Tuple2<Double, Node<T>>> closestN = inRange.stream()
                .map(node -> new Tuple2<>(env.getDistanceBetweenNodes(center, node), node))
                .collect(new SmallestN<>(n));
        setRange(center, Math.max(Double.MIN_VALUE, closestN.peekLast().v1()) * CONNECTION_RANGE_TOLERANCE);
        return closestN.stream().map(Tuple2::v2);
    }

    /**
     * @param env the {@link Environment}
     * @param node the {@link Node}
     * @param range the communication range
     * @return the set of nodes within the communication range
     */
    protected final Set<Node<T>> nodesInRange(final Environment<T, ?> env, final Node<T> node, final double range) {
        return env.getNodesWithinRange(node, range);
    }

    /**
     * This method always return true. Subclasses can override it.
     * 
     * @param node
     *            the node
     * @return true if the node is enabled (can be linked).
     */
    protected boolean nodeIsEnabled(final Node<T> node) {
        return true;
    }

    /**
     * Gets the communication range of a node.
     * 
     * @param env
     *            the environment
     * @param center
     *            the node
     * @return the communication range
     */
    protected final double getRange(final Environment<T, ?> env, final Node<T> center) {
        try {
            /*
             * Range estimation: twice the radius of a circle with an area that
             * would, on average, contain the number of required devices
             */
            return ranges.get(center, () -> {
                final int nodes = env.getNodesNumber();
                if (nodes < n || nodes < 10) {
                    return Double.MAX_VALUE;
                }
                final double[] size = env.getSizeInDistanceUnits();
                final double x = size[0];
                final double y = size[1];
                final double density = x * y / nodes;
                return Math.max(Double.MIN_VALUE, Math.min(2 * FastMath.sqrt(density / Math.PI * n), Double.MAX_VALUE));
            });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Couldn't compute ranges. This is most likely a bug.", e);
        }
    }

    /**
     * Sets a communication range for a node. Used for optimization purposes.
     * 
     * @param center
     *            the node
     * @param range
     *            the range
     */
    protected final void setRange(final Node<T> center, final double range) {
        ranges.put(center, range);
    }

    @Override
    public final boolean isLocallyConsistent() {
        return false;
    }

    /**
     * @return the number of neighbors
     */
    protected final int getN() {
        return n;
    }

}
