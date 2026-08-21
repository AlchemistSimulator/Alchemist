/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.Position;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nonnull;

/**
 * Moves the node randomly.
 *
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public final class BrownianMove<T, P extends Position<P>> extends AbstractMoveNode<T, P> {

    private final double range;
    private final RandomGenerator randomGenerator;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param randomGenerator
     *            the simulation {@link RandomGenerator}.
     * @param range
     *            the maximum distance the node may walk in a single step for
     *            each dimension
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public BrownianMove(
        final Environment<T, P> environment,
        final Node<T> node,
        final RandomGenerator randomGenerator,
        final double range
    ) {
        super(environment, node);
        this.range = range;
        this.randomGenerator = randomGenerator;
    }

    @Nonnull
    @Override
    public Action<T> cloneAction(@Nonnull final Node<T> node, @Nonnull final NodeReaction<T> reaction) {
        return new BrownianMove<>(getEnvironment(), node, randomGenerator, range);
    }

    @Override
    public P getNextPosition() {
        return getEnvironment().makePosition(genRandom() * range, genRandom() * range);
    }

    private double genRandom() {
        return randomGenerator.nextFloat() - 0.5;
    }
}
