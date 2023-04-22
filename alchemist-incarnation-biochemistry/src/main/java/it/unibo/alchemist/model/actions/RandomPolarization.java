/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.actions;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.Objects;

/**
 * Polarizes a {@link CellProperty} in a random direction.
 */
public final class RandomPolarization extends AbstractRandomizableAction<Double> {

    private static final long serialVersionUID = 1L;
    private final Environment<Double, Euclidean2DPosition> environment;
    private final CellProperty<Euclidean2DPosition> cell;

    /**
     * @param environment the environment
     * @param node the node
     * @param random the {@link RandomGenerator}
     */
    public RandomPolarization(
        final Environment<Double, Euclidean2DPosition> environment,
        final Node<Double> node,
        final RandomGenerator random
    ) {
        super(node, random);
        this.environment = environment;
        this.cell = node.asPropertyOrNull(CellProperty.class);
        Objects.requireNonNull(
            cell,
            "Polarization can happen only in nodes with " + CellProperty.class.getSimpleName()
        );
    }

    /**
     * 
     */
    @Override
    public void execute() {
        final double x = getRandomGenerator().nextFloat() - 0.5;
        final double y = getRandomGenerator().nextFloat() - 0.5;
        Euclidean2DPosition randomVersor;
        if (x == 0) {
            randomVersor = new Euclidean2DPosition(0, 1);
        } else if (y == 0) {
            randomVersor = new Euclidean2DPosition(1, 0);
        } else {
            final double module = FastMath.sqrt(FastMath.pow(x, 2) + FastMath.pow(y, 2));
            if (module == 0) {
                randomVersor = new Euclidean2DPosition(0, 0);
            } else {
                randomVersor = new Euclidean2DPosition(x / module, y / module);
            }
        }
        cell.addPolarizationVersor(randomVersor);
    }

    /**
     * 
     */
    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public RandomPolarization cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new RandomPolarization(environment, node, getRandomGenerator());
    }

}
