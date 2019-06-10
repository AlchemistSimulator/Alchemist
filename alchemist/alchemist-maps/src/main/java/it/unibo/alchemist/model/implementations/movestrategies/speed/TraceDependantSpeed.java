/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.speed;

import java.util.Objects;

import it.unibo.alchemist.model.implementations.movestrategies.AbstractStrategyWithGPS;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;

/**
 * This strategy dynamically tries to move the node adjusting its speed to
 * synchronize the reaction rate and the traces data.
 *
 * @param <T>
 */
public abstract class TraceDependantSpeed<T> extends AbstractStrategyWithGPS implements SpeedSelectionStrategy<GeoPosition> {

    private static final long serialVersionUID = 8021140539083062866L;
    private final Reaction<T> reaction;
    private final MapEnvironment<T> env;
    private final Node<T> node;

    /**
     * @param e
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     */
    public TraceDependantSpeed(final MapEnvironment<T> e, final Node<T> n, final Reaction<T> r) {
        env = Objects.requireNonNull(e);
        node = Objects.requireNonNull(n);
        reaction = Objects.requireNonNull(r);
    }

    @Override
    public final double getNodeMovementLength(final GeoPosition target) {
        final Time currentTime = reaction.getTau();
        final double curTime = currentTime.toDouble();
        final GPSPoint next = getTrace().getNextPosition(currentTime);
        final double expArrival = next.getTime().toDouble();
        if (curTime >= expArrival) {
            return Double.POSITIVE_INFINITY;
        }
        final double frequency = reaction.getRate();
        final double steps = (expArrival - curTime) * frequency;
        return computeDistance(env, node, target) / steps;
    }

    /**
     * @param environment
     *            the environment
     * @param curNode
     *            the node
     * @param targetPosition
     *            the target
     * @return an estimation of the distance between the node and the target
     *         position
     */
    protected abstract double computeDistance(MapEnvironment<T> environment, Node<T> curNode, GeoPosition targetPosition);
}
