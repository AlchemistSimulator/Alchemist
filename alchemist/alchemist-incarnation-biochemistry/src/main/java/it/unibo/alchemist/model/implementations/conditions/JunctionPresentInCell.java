/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.Collections;

/**
 */
public final class JunctionPresentInCell extends AbstractNeighborCondition<Double> {

    private static final long serialVersionUID = 4213307452790768059L;

    private final Junction j;
    private final Environment<Double, ?> env;

    /**
     * 
     * @param junction the junction
     * @param n the node
     * @param e the environment
     */
    public JunctionPresentInCell(final Environment<Double, ?> e, final Node<Double> n, final Junction junction) {
        super(e, n);
        if (n instanceof CellNode) {
            declareDependencyOn(junction);
            j = junction;
            env = e;
        } else {
            throw new UnsupportedOperationException("This Condition can be set only in CellNodes");
        }
    }

    @Override
    public boolean isValid() {
        return getNode().containsJunction(j);
    }

    @Override
    public JunctionPresentInCell cloneCondition(final Node<Double> n, final Reaction<Double> r) {
        return new JunctionPresentInCell(env, n, j);
    }

    @Override
    protected double getNeighborPropensity(final Node<Double> neighbor) {
        // the neighbor's propensity is computed as the number of junctions it has
        //noinspection SuspiciousMethodCalls
        return getNode().getJunctions()
                .getOrDefault(j, Collections.emptyMap())
                .getOrDefault(neighbor, 0);
    }

    @Override
    public String toString() {
        return "junction " +  j.toString() + " present ";
    }

    @Override
    public CellNode<?> getNode() {
        return (CellNode<?>) super.getNode();
    }

}
