/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Moves the node randomly.
 * 
 * @param <T>
 * @param <P>
 */
public final class BrownianMove<T, P extends Position<P>> extends AbstractMoveNode<T, P> {

    private static final long serialVersionUID = -904100978119782403L;
    private final double r;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator rng;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param rand
     *            the simulation {@link RandomGenerator}.
     * @param range
     *            the maximum distance the node may walk in a single step for
     *            each dimension
     */
    public BrownianMove(final Environment<T, P> environment, final Node<T> node, final RandomGenerator rand, final double range) {
        super(environment, node);
        r = range;
        rng = rand;
    }

    @Override
    public Action<T> cloneAction(final Node<T> n, final Reaction<T> reaction) {
        return new BrownianMove<>(getEnvironment(), n, rng, r);
    }

    @Override
    public P getNextPosition() {
        return getEnvironment().makePosition(genRandom() * r, genRandom() * r);
    }

    private double genRandom() {
        return rng.nextFloat() - 0.5;
    }

    /**
     * @return the movement radius
     */
    protected double getRadius() {
        return r;
    }

    /**
     * @return the {@link RandomGenerator}
     */
    protected RandomGenerator getRandomGenerator() {
        return rng;
    }

}
