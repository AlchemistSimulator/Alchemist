/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serial;

/**
 * Moves the node randomly.
 *
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public final class BrownianMove<T, P extends Position<P>> extends AbstractMoveNode<T, P> {

    @Serial
    private static final long serialVersionUID = -904100978119782403L;
    private final double range;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
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

    @Override
    public Action<T> cloneAction(final Node<T> node, final Reaction<T> reaction) {
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
