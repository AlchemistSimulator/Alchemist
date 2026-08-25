/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.conditions.AbstractCondition;

import javax.annotation.Nonnull;

/**
 *
 *
 */
public final class EnvPresent extends AbstractCondition<Double> {

    /**
     *
     */

    private final Environment<Double, ?> environment;

    /**
     * @param node the node
     * @param environment the environment
     */
    public EnvPresent(final Environment<Double, ?> environment, final Node<Double> node) {
        super(node);
        this.environment = environment;
        addObservableDependency(environment.getNeighborhood(node));
        setValidity(environment.getNeighborhood(node).map(it ->
            it.getNeighbors().stream().anyMatch(n -> n instanceof EnvironmentNode)
        ));
        setPropensityContribution(isValid().map(it -> it ? 1d : 0d));
    }

    @Nonnull
    @Override
    public EnvPresent cloneCondition(
        @Nonnull final Node<Double> newNode,
        @Nonnull final NodeReaction<Double> newReaction
    ) {
        return new EnvPresent(environment, newNode);
    }

    @Override
    public String toString() {
        return "has environment [" + isValid() + "]";
    }
}
