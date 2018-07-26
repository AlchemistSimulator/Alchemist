/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <T>
 */
public class SelectiveAdaptiveRange<T, P extends Position<P>> extends AdaptiveRange<T, P> {

    /**
     * The default filter molecule.
     */
    public static final String DEFAULT_MOLECULETYPE = "infrastructure";
    private static final long serialVersionUID = 8301318269785386062L;

    private final ILsaMolecule moleculeType;

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
    public SelectiveAdaptiveRange(final double radius, final double minrange, final double maxrange, final int num, final int tolerance) {
        this(radius, minrange, maxrange, num, tolerance, DEFAULT_ADJUSTMENT, DEFAULT_MOLECULETYPE);
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
    public SelectiveAdaptiveRange(final double radius, final double minrange, final double maxrange, final int num, final int tolerance, final double adjustment) {
        this(radius, minrange, maxrange, num, tolerance, adjustment, DEFAULT_MOLECULETYPE);
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
     * @param molType
     *            the molecule whose presence will allow links to be created
     */
    public SelectiveAdaptiveRange(final double radius, final double minrange, final double maxrange, final int num, final int tolerance, final double adjustment, final String molType) {
        super(radius, minrange, maxrange, num, tolerance, adjustment);
        moleculeType = new LsaMolecule(molType);
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
    public SelectiveAdaptiveRange(final double radius, final double minrange, final int num, final int tolerance) {
        this(radius, minrange, DEFAULT_MAXRANGE, num, tolerance, DEFAULT_ADJUSTMENT, DEFAULT_MOLECULETYPE);
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
    public SelectiveAdaptiveRange(final double radius, final double minrange, final int num, final int tolerance, final double adjustment) {
        this(radius, minrange, DEFAULT_MAXRANGE, num, tolerance, adjustment, DEFAULT_MOLECULETYPE);
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
    public SelectiveAdaptiveRange(final double radius, final int num, final int tolerance) {
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
    public SelectiveAdaptiveRange(final double radius, final int num, final int tolerance, final double adjustment) {
        this(radius, DEFAULT_MINRANGE, DEFAULT_MAXRANGE, num, tolerance, adjustment, DEFAULT_MOLECULETYPE);
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
     * @param molType
     *            the molecule whose presence will allow links to be created
     */
    public SelectiveAdaptiveRange(final double radius, final int num, final int tolerance, final String molType) {
        this(radius, DEFAULT_MINRANGE, DEFAULT_MAXRANGE, num, tolerance, DEFAULT_ADJUSTMENT, molType);
    }

    /**
     * Acts as a filter. It is called to test if the nodes in the computed
     * neighborhood (namely, those nodes within the communication range of the
     * current node) should be removed or not, and must return true if the node
     * should not be inserted in the neighborhood. This implementation checks
     * that the actual distance between the nodes is shorter than the
     * communication range of the neighbor.
     * 
     * @param env
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
    protected boolean conditionForRemoval(final Environment<T, P> env, final Node<T> center, final Node<T> neighbor, final double centerRange, final double neighRange) {
        return !neighbor.contains(moleculeType) || super.conditionForRemoval(env, center, neighbor, centerRange, neighRange);
    }
}
