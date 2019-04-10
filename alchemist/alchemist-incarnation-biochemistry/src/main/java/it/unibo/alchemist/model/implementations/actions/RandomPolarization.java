/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * 
 */
public class RandomPolarization extends AbstractRandomizableAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * 
     * @param node 
     * @param random 
     */
    public RandomPolarization(final Node<Double> node, final RandomGenerator random) {
        super(node, random);
        if (!(node instanceof CellNode)) {
            throw  new UnsupportedOperationException("Polarization can happen only in cells.");
        }
    }

    /**
     * 
     */
    @Override
    public void execute() {
        final double x = getRandomGenerator().nextFloat() - 0.5;
        final double y = getRandomGenerator().nextFloat() - 0.5;
        Position randomVersor = new Euclidean2DPosition(x, y);
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
        getNode().addPolarization(randomVersor);
    }

    /**
     * 
     */
    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public RandomPolarization cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new RandomPolarization(n, getRandomGenerator());
    }

    @Override 
    public CellNode getNode()  {
        return (CellNode) super.getNode();
    }

}
