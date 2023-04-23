/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.linkingrules;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.primitives.Doubles;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.util.BugReporting;
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.LinkingRule;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import org.apache.commons.math3.util.FastMath;
import org.danilopianini.util.stream.SmallestN;
import org.jooq.lambda.tuple.Tuple2;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Non local-consistent rule that connect the closest N nodes together.
 * Two nodes get connected if either one belongs to the set of the ten devices closest to the other.
 * 
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public class ClosestN<T, P extends Position<P>> implements LinkingRule<T, P> {

    private static final long serialVersionUID = 2L;
    private static final double CONNECTION_RANGE_TOLERANCE = 1.1;
    private final int n, expectedNodes, maxNodes;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Cache<Node<T>, Double> ranges;

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
        this.n = n;
        this.expectedNodes = expectedNodes;
        this.maxNodes = maxNodes;
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

    private Cache<Node<T>, Double> ranges() {
        if (ranges == null) {
            ranges = CacheBuilder.newBuilder()
                .maximumSize(maxNodes)
                .build();
        }
        return ranges;
    }

    @Override
    public final Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> environment) {
        if (environment.getNodeCount() < expectedNodes || !nodeIsEnabled(center)) {
            return Neighborhoods.make(environment, center);
        }
        return Neighborhoods.make(environment, center,
                Stream.concat(
                    closestN(center, environment),
                    /*
                     * Of all nodes but myself...
                     */
                    environment.getNodes().parallelStream()
                        /*
                         * ...select those for which I'm on the closest n
                         */
                        .filter(node -> 
                                !center.equals(node)
                                && closestN(node, environment).anyMatch(center::equals)
                        )
                )
                .sequential()
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private Stream<Node<T>> closestN(final Node<T> center, final Environment<T, ?> environment) {
        if (!nodeIsEnabled(center)) {
            return Stream.empty();
        }
        double currentRange = getRange(environment, center);
        Set<Node<T>> inRange;
        final double maxRange = Doubles.max(environment.getSizeInDistanceUnits()) * 2;
        do {
            inRange = (environment.getNodeCount() > n && currentRange < maxRange
                    ? nodesInRange(environment, center, currentRange).stream()
                    : environment.getNodes().stream())
                        .filter(n -> !n.equals(center) && nodeIsEnabled(n))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            currentRange *= 2;
        } while (inRange.size() < n && inRange.size() < environment.getNodeCount() - 1 && currentRange < maxRange * 2);
        if (inRange.isEmpty()) {
            return Stream.empty();
        }
        final MinMaxPriorityQueue<Tuple2<Double, Node<T>>> closestN = inRange.stream()
                .map(node -> new Tuple2<>(environment.getDistanceBetweenNodes(center, node), node))
                .collect(new SmallestN<>(n));
        final var farthestNode = closestN.peekLast();
        if (farthestNode == null) {
            final var debugDetails = new HashMap<String, Object>();
            debugDetails.put("farthestNode", null);
            debugDetails.put("inRange", inRange);
            debugDetails.put("closestN", closestN);
            BugReporting.reportBug(
                "neighbors were found, but no neighbor was included in the list",
                debugDetails
            );
        }
        setRange(center, Math.max(Double.MIN_VALUE, farthestNode.v1()) * CONNECTION_RANGE_TOLERANCE);
        return closestN.stream().map(Tuple2::v2);
    }

    /**
     * The set of nodes within the comunication range.
     * @param environment the {@link Environment}
     * @param node the {@link Node}
     * @param range the communication range
     * @return the set of nodes within the communication range
     */
    protected final Set<Node<T>> nodesInRange(
            final Environment<T, ?> environment,
            final Node<T> node, final double range
    ) {
        return environment.getNodesWithinRange(node, range);
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
     * @param environment
     *            the environment
     * @param center
     *            the node
     * @return the communication range
     */
    protected final double getRange(final Environment<T, ?> environment, final Node<T> center) {
        try {
            /*
             * Range estimation: twice the radius of a circle with an area that
             * would, on average, contain the number of required devices
             */
            return ranges().get(center, () -> {
                final int nodes = environment.getNodeCount();
                if (nodes < n || nodes < 10) {
                    return Double.MAX_VALUE;
                }
                final double[] size = environment.getSizeInDistanceUnits();
                final double x = size[0];
                final double y = size[1];
                final double density = x * y / nodes;
                return Math.max(Double.MIN_VALUE,
                        Math.min(2 * FastMath.sqrt(density / Math.PI * n), Double.MAX_VALUE)
                );
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
        ranges().put(center, range);
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
