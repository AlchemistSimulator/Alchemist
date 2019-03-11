/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * 
 *
 */
public class EnvPresent extends AbstractCondition<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Environment<Double, ?> environment;

    /**
     * 
     * @param node 
     * @param env 
     */
    public EnvPresent(final Environment<Double, ?> env, final Node<Double> node) {
        super(node);
        environment = env;
    }

    @Override
    public EnvPresent cloneCondition(final Node<Double> n, final Reaction<Double> r) {
        return new EnvPresent(environment, n);
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
