/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * @param <T>
 *            Concentration type
 * @param <P>
 *            Position type
 */
public final class MoveForwardAndTeleport<T, P extends Position2D<P>> extends AbstractMoveNode<T, P> {

    private static final long serialVersionUID = 6853946136578807021L;
    private final double dx, minx, maxx;
    private double y = Double.NaN;

    /**
     * @param environment the environment
     * @param node the node
     * @param deltaX how far along the x axis the node should move each time the action is triggered
     * @param minX minimum x point
     * @param maxX maximum x point
     */
    public MoveForwardAndTeleport(final Environment<T, P> environment, final Node<T> node, final double deltaX, final double minX, final double maxX) {
        super(environment, node, true);
        dx = deltaX;
        minx = minX;
        maxx = maxX;
    }

    @Override
    public MoveForwardAndTeleport<T, P> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new MoveForwardAndTeleport<>(getEnvironment(), n, dx, minx, maxx);
    }

    @Override
    public P getNextPosition() {
        final P cur = getEnvironment().getPosition(getNode());
        if (Double.isNaN(y)) {
            y = cur.getY();
        }
        final double x = cur.getX();
        if (x > maxx) {
            return getEnvironment().makePosition(minx, y);
        }
        return getEnvironment().makePosition(x + dx, y);
    }

}
