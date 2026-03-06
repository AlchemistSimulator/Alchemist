/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.conditions.AbstractCondition;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.observation.ObservableExtensions;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a condition on a neighbor.
 * Formally, this condition is satisfied
 * if at least one neighbor satisfies the condition.
 *
 * @param <T> the concentration type.
 */
public abstract class AbstractNeighborCondition<T> extends AbstractCondition<T> {

    @Serial
    private static final long serialVersionUID = 1133243697147282024L;

    private final Environment<T, ?> environment;

    /**
     * @param node        the node hosting this condition
     * @param environment the current environment
     */
    protected AbstractNeighborCondition(final Environment<T, ?> environment, final Node<T> node) {
        super(node);
        this.environment = environment;
        addObservableDependency(getEnvironment().getNeighborhood(getNode()));
        setPropensityContributionObservable();
    }

    @Override
    public abstract AbstractNeighborCondition<T> cloneCondition(Node<T> newNode, Reaction<T> newReaction);

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
     * Override if the desired behavior differs. Default is returning the sum of the neighbor's propensities
     */
    protected void setPropensityContributionObservable() {
        setPropensity(observeValidNeighbors().map(nodes ->
            nodes.values().stream().mapToDouble(it -> it).sum()
        ));
    }

    /**
     * Searches in the given neighborhood which nodes satisfy the condition and
     * returns a list of valid neighbors. NOTE, it is NOT guaranteed that this
     * method checks if the passed neighborhood is the actual neighborhood of the
     * node. Make sure the passed neighborhood is up to date to avoid problems.
     *
     * @return a map of neighbors which satisfy the condition and their propensity
     */
    public final Map<Node<T>, Double> getValidNeighbors() {
        return observeValidNeighbors().getCurrent();
    }

    /**
     * Searches in the given neighborhood which nodes satisfy the condition and
     * returns a list of valid neighbors as an {@link Observable}. The observed map
     * emits if:
     * <ul>
     *     <li>Neighbors belonging to the neighborhood of the node associated with this condition changes;</li>
     *     <li>Some neighbor propensity (observed through {@link #observeNeighborPropensity(Node)}) changes.</li>
     * </ul>
     *
     * @return an observable which emits a map of neighbors which satisfy the condition and their propensity.
     */
    public final Observable<Map<Node<T>, Double>> observeValidNeighbors() {
        return ObservableExtensions.INSTANCE.switchMap(
            getEnvironment().getNeighborhood(getNode()),
            neighborhood -> {
                final List<Observable<Pair<Node<T>, Double>>> propensities = neighborhood.getNeighbors().stream()
                    .map((Node<T> neighbor) -> observeNeighborPropensity(neighbor)
                        .map(prop -> (Pair<Node<T>, Double>) new ImmutablePair<>(neighbor, prop))).toList();

                return ObservableExtensions.INSTANCE.combineLatest(
                    propensities,
                    list -> list.stream()
                        .filter(pair -> pair.getValue() > 0)
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                ).map(it -> it.fold(Map::of, val -> val));
            }
        );
    }

    /**
     * Given a node, which is supposed to be in the neighborhood of the current node, the function computes a double
     * value representing the propensity of the neighbor to be the chosen one for the reaction to be executed.
     * The value returned must be `0` if the neighbor is not eligible for the reaction due to this condition.
     * This value could be used to compute the reaction's propensity, but the main usage is to give a rate to
     * every neighbor and randomly choose one of them.
     *
     * @param neighbor - the neighbor whose propensity to be chosen has to be computed
     * @return an observable of the neighbor's propensity to be chosen as the other node of the reaction
     */
    protected abstract Observable<Double> observeNeighborPropensity(Node<T> neighbor);
}
