/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions;

import java.util.Map;
import java.util.stream.Collectors;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Represents a condition on a neighbor. Formally this conditions is satisfied
 * if at least one neighbor satisfy the condition.
 * 
 * @param <T>
 *            the concentration type.
 */
public abstract class AbstractNeighborCondition<T> extends AbstractCondition<T> {

    private static final long serialVersionUID = 1133243697147282024L;

    private final Environment<T, ?> environment;

    /**
     * 
     * @param node
     *            the node hosting this condition
     * @param environment
     *            the current environment
     */
    protected AbstractNeighborCondition(final Environment<T, ?> environment, final Node<T> node) {
        super(node);
        this.environment = environment;
    }

    @Override
    public abstract AbstractNeighborCondition<T> cloneCondition(Node<T> node, Reaction<T> reaction);

    @Override
    public final Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return allows subclasses to access the environment
     */
    protected final Environment<T, ?> getEnvironment() {
        return environment;
    }

    /**
     * Override if desired behavior differs. Default is returning the sum of the neighbor's propensities
     * @return the sum of the neighbor's propensities
     */
    @Override
    public double getPropensityContribution() {
        // the condition's propensity contribution is computed as the sum of the neighbor's propensities
        return getValidNeighbors().values().stream().mapToDouble(it -> it).sum();
    }

    /**
     * Searches in the given neighborhood which nodes satisfy the condition, and
     * returns a list of valid neighbors. NOTE, it is NOT guaranteed that this
     * method checks if the passed neighborhood is the actual neighborhood of the
     * node. Make sure the passed neighborhood is up to date for avoid problems.
     * 
     * @return a map of neighbors which satisfy the condition and their propensity
     */
    public final Map<Node<T>, Double> getValidNeighbors() {
        return getEnvironment().getNeighborhood(getNode()).getNeighbors().stream()
                .map(it -> new ImmutablePair<>(it, getNeighborPropensity(it)))
                .filter(it -> it.getValue() > 0)
                .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue));
    }

    /**
     * Given a node, which is supposed to be in the neighborhood of the current node, the function computes a double
     * value representing the propensity of the neighbor to be the chosen one for the reaction to be executed.
     * The value returned must be 0 if the neighbor is not eligible for the reaction due to this condition.
     * This value could be used to compute the reaction's propensity, but the main usage is to give a rate to
     * every neighbor and randomly choose one of them.
     *
     * @param neighbor - the neighbor whose propensity to be chosen has to be computed
     *
     * @return the neighbor's propensity to be chosen as the other node of the reaction
     */
    protected abstract double getNeighborPropensity(Node<T> neighbor);
}
