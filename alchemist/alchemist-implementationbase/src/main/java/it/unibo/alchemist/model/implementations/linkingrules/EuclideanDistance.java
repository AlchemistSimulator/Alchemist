/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.implementations.neighborhoods.CachedNeighborhood;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * LinkingRule which connects nodes whose euclidean distance is shorter than a
 * given radius.
 * 
 * @param <T>
 *            The type which describes the concentration of a molecule
 */
public class EuclideanDistance<T> extends AbstractLocallyConsistentLinkingRule<T> {

    private static final long serialVersionUID = -405055780667941773L;
    private final double range;

    /**
     * @param radius
     *            connection radius
     */
    public EuclideanDistance(final double radius) {
        range = radius;
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T> env) {
        return new CachedNeighborhood<>(center, env.getNodesWithinRange(center, range), env);
    }

    /**
     * @return the range
     */
    protected final double getRange() {
        return range;
    }

}
