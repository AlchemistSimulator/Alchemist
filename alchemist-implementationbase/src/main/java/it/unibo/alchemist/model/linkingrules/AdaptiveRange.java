/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.linkingrules;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import org.danilopianini.util.ListSet;

import java.util.stream.Collectors;

/**
 * This linking rule dynamically searches for the best radius for each device,
 * in such a way that it connects to a certain number of devices.
 *
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public class AdaptiveRange<T, P extends Position<P>> extends ConnectWithinDistance<T, P> {

    /**
     * Default adjustment.
     */
    public static final double DEFAULT_ADJUSTMENT = 1d;
    /**
     * Default maximum range.
     */
    public static final double DEFAULT_MAXRANGE = 100d;
    /**
     * Default minimum range.
     */
    public static final double DEFAULT_MINRANGE = 1d;
    private static final long serialVersionUID = 8301318269785386062L;
    private final double defaultAdjustment, minRange, maxRange;
    private final int n, t;
    private final TIntDoubleMap ranges = new TIntDoubleHashMap();

    /**
     * @param radius
     *            default radius in metres
     * @param minrange
     *            minimum radius in metres
     * @param maxrange
     *            maximum radius in metres
     * @param num
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     */
    public AdaptiveRange(final double radius, final double minrange, final double maxrange, final int num, final int tolerance) {
        this(radius, minrange, maxrange, num, tolerance, DEFAULT_ADJUSTMENT);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minrange
     *            minimum radius in metres
     * @param maxrange
     *            maximum radius in metres
     * @param num
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of the
     *            bounds
     */
    public AdaptiveRange(
            final double radius,
            final double minrange,
            final double maxrange,
            final int num,
            final int tolerance,
            final double adjustment
    ) {
        super(radius);
        n = Math.max(num, 0);
        t = tolerance;
        defaultAdjustment = adjustment;
        minRange = minrange;
        maxRange = maxrange;
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minrange
     *            minimum radius in metres
     * @param num
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     */
    public AdaptiveRange(final double radius, final double minrange, final int num, final int tolerance) {
        this(radius, minrange, DEFAULT_MAXRANGE, num, tolerance, DEFAULT_ADJUSTMENT);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minrange
     *            minimum radius in metres
     * @param num
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of the
     *            bounds
     */
    public AdaptiveRange(
            final double radius,
            final double minrange,
            final int num,
            final int tolerance,
            final double adjustment
    ) {
        this(radius, minrange, DEFAULT_MAXRANGE, num, tolerance, adjustment);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param num
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     */
    public AdaptiveRange(final double radius, final int num, final int tolerance) {
        this(radius, DEFAULT_MINRANGE, num, tolerance);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param num
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of the
     *            bounds
     */
    public AdaptiveRange(final double radius, final int num, final int tolerance, final double adjustment) {
        this(radius, DEFAULT_MINRANGE, DEFAULT_MAXRANGE, num, tolerance, adjustment);
    }

    @Override
    public final Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> environment) {
        if (!ranges.containsKey(center.getId())) {
            ranges.put(center.getId(), getRange());
        }
        final double curRange = ranges.get(center.getId());
        final ListSet<Node<T>> potentialNeighs = environment.getNodesWithinRange(center, curRange);
        final Neighborhood<T> neigh = Neighborhoods.make(environment, center, potentialNeighs.stream()
                .filter(neighbor -> !conditionForRemoval(environment, center, neighbor, curRange, ranges.get(neighbor.getId())))
                .collect(Collectors.toList()));
        if (neigh.size() > n + t) {
            ranges.put(center.getId(), Math.max(curRange - defaultAdjustment, minRange));
        } else if (neigh.size() < Math.max(1, n - t)) {
            ranges.put(center.getId(), Math.min(curRange + defaultAdjustment, maxRange));
        }
        return neigh;
    }

    /**
     * Acts as a filter.
     * It is called to test if the nodes in the computed neighborhood
     * (namely, those nodes within the communication range of the current node)
     * should be removed or not, and must return true if the node should not be inserted in the neighborhood.
     * This implementation checks that the actual distance between the nodes is shorter
     * than the communication range of the neighbor.
     * 
     * @param environment the current environment
     * @param center the current node
     * @param neighbor the neighbor to test
     * @param centerRange the communication range of the current node
     * @param neighRange the communication range of the neighbor
     * @return true if the node must be removed, false otherwise
     */
    protected boolean conditionForRemoval(
            final Environment<T, P> environment,
            final Node<T> center,
            final Node<T> neighbor,
            final double centerRange,
            final double neighRange
    ) {
        return environment.getDistanceBetweenNodes(center, neighbor) > neighRange;
    }

}
