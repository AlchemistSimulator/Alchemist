/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * LinkingRule which connects nodes whose euclidean distance is shorter than a
 * given radius.
 * 
 * @param <T>
 *            The type which describes the concentration of a molecule
 * @param <P>
 */
public class ConnectWithinDistance<T, P extends Position<P>> extends AbstractLocallyConsistentLinkingRule<T, P> {

    private static final long serialVersionUID = -405055780667941773L;
    private final double range;

    /**
     * @param radius
     *            connection radius
     */
    public ConnectWithinDistance(final double radius) {
        range = radius;
    }

    /**
     * Subclasses may change the way a neighborhood is computed.
     */
    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> env) {
        return Neighborhoods.make(env, center, env.getNodesWithinRange(center, range));
    }

    /**
     * @return the range
     */
    protected final double getRange() {
        return range;
    }

}
