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
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.conditions.AbstractCondition;

/**
 * 
 *
 */
public final class EnvPresent extends AbstractCondition<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Environment<Double, ?> environment;

    /**
     * 
     * @param node the node
     * @param environment the environment
     */
    public EnvPresent(final Environment<Double, ?> environment, final Node<Double> node) {
        super(node);
        this.environment = environment;
    }

    @Override
    public EnvPresent cloneCondition(final Node<Double> node, final Reaction<Double> reaction) {
        return new EnvPresent(environment, node);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityContribution() {
        return isValid() ? 1d : 0d;
    }

    @Override
    public boolean isValid() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .anyMatch(n -> n instanceof EnvironmentNode);
    }

    @Override
    public String toString() {
        return "has environment [" + isValid() + "]";
    }
}
