/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.linkingrules;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.linkingrules.AdaptiveRange;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;

import java.io.Serial;

/**
 * @param <P> position type
 * @param <T> concentration type
 */
public class SelectiveAdaptiveRange<T, P extends Position<P>> extends AdaptiveRange<T, P> {

    /**
     * The default filter molecule.
     */
    public static final String DEFAULT_MOLECULETYPE = "infrastructure";
    @Serial
    private static final long serialVersionUID = 8301318269785386062L;

    private final ILsaMolecule moleculeType;

    /**
     * @param radius
     *            default radius in metres
     * @param minRange
     *            minimum radius in metres
     * @param maxRange
     *            maximum radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than num-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            num+tolerance, the radius is decreased
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final double minRange,
            final double maxRange,
            final int desiredNeighborsCount,
            final int tolerance
    ) {
        this(radius, minRange, maxRange, desiredNeighborsCount, tolerance, DEFAULT_ADJUSTMENT, DEFAULT_MOLECULETYPE);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minRange
     *            minimum radius in metres
     * @param maxRange
     *            maximum radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of bounds
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final double minRange,
            final double maxRange,
            final int desiredNeighborsCount,
            final int tolerance,
            final double adjustment
    ) {
        this(radius, minRange, maxRange, desiredNeighborsCount, tolerance, adjustment, DEFAULT_MOLECULETYPE);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minRange
     *            minimum radius in metres
     * @param maxRange
     *            maximum radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of bounds
     * @param molType
     *            the molecule whose presence will allow links to be created
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final double minRange,
            final double maxRange,
            final int desiredNeighborsCount,
            final int tolerance,
            final double adjustment,
            final String molType
    ) {
        super(radius, minRange, maxRange, desiredNeighborsCount, tolerance, adjustment);
        moleculeType = new LsaMolecule(molType);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minRange
     *            minimum radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final double minRange,
            final int desiredNeighborsCount,
            final int tolerance
    ) {
        this(radius, minRange, DEFAULT_MAXRANGE, desiredNeighborsCount, tolerance, DEFAULT_ADJUSTMENT, DEFAULT_MOLECULETYPE);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param minRange
     *            minimum radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of bounds
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final double minRange,
            final int desiredNeighborsCount,
            final int tolerance,
            final double adjustment
    ) {
        this(radius, minRange, DEFAULT_MAXRANGE, desiredNeighborsCount, tolerance, adjustment, DEFAULT_MOLECULETYPE);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     */
    public SelectiveAdaptiveRange(final double radius, final int desiredNeighborsCount, final int tolerance) {
        this(radius, DEFAULT_MINRANGE, desiredNeighborsCount, tolerance);
    }

    /**
     * @param radius
     *            default radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     * @param adjustment
     *            the amount of metres the range will be changed if out of bounds
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final int desiredNeighborsCount,
            final int tolerance,
            final double adjustment
    ) {
        this(
                radius,
                DEFAULT_MINRANGE,
                DEFAULT_MAXRANGE,
                desiredNeighborsCount,
                tolerance,
                adjustment,
                DEFAULT_MOLECULETYPE
        );
    }

    /**
     * @param radius
     *            default radius in metres
     * @param desiredNeighborsCount
     *            preferred number of neighbors
     * @param tolerance
     *            if the number of neighbors is smaller than desiredNeighborsCount-tolerance, the
     *            radius is increased; if the number of neighbors is higher than
     *            desiredNeighborsCount+tolerance, the radius is decreased
     * @param molType
     *            the molecule whose presence will allow links to be created
     */
    public SelectiveAdaptiveRange(
            final double radius,
            final int desiredNeighborsCount,
            final int tolerance,
            final String molType
    ) {
        this(
                radius,
                DEFAULT_MINRANGE,
                DEFAULT_MAXRANGE,
                desiredNeighborsCount,
                tolerance,
                DEFAULT_ADJUSTMENT,
                molType
        );
    }

    /**
     * Acts as a filter.
     * Tests whether nodes in the computed
     * neighborhood (namely, those nodes within the communication range of the
     * current node) should be removed or not, and must return true if the node
     * should not be inserted in the neighborhood.
     * This implementation checks
     * that the actual distance between the nodes is shorter than the
     * communication range of the neighbor.
     *
     * @param environment
     *            the current environment
     * @param center
     *            the current node
     * @param neighbor
     *            the neighbor to test
     * @param centerRange
     *            the communication range of the current node
     * @param neighRange
     *            the communication range of the neighbor
     * @return true if the node must be removed, false otherwise
     */
    @Override
    protected boolean conditionForRemoval(
            final Environment<T, P> environment,
            final Node<T> center,
            final Node<T> neighbor,
            final double centerRange,
            final double neighRange
    ) {
        return !neighbor.contains(moleculeType)
                || super.conditionForRemoval(environment, center, neighbor, centerRange, neighRange);
    }
}
