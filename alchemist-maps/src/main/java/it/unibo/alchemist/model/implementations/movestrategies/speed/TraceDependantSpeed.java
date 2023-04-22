/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.speed;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.movestrategies.AbstractStrategyWithGPS;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.interfaces.RoutingService;
import it.unibo.alchemist.model.interfaces.RoutingServiceOptions;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy;

import java.util.Objects;

/**
 * This strategy dynamically tries to move the node adjusting its speed to
 * synchronize the reaction rate and the traces data.
 *
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
 */
public abstract class TraceDependantSpeed<T, O extends RoutingServiceOptions<O>, S extends RoutingService<GeoPosition, O>>
    extends AbstractStrategyWithGPS
    implements SpeedSelectionStrategy<T, GeoPosition> {

    private static final long serialVersionUID = 8021140539083062866L;
    private final Reaction<T> reaction;
    private final MapEnvironment<T, O, S> environment;
    private final Node<T> node;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is made by purpose")
    public TraceDependantSpeed(final MapEnvironment<T, O, S> environment, final Node<T> node, final Reaction<T> reaction) {
        this.environment = Objects.requireNonNull(environment);
        this.node = Objects.requireNonNull(node);
        this.reaction = Objects.requireNonNull(reaction);
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
        return computeDistance(environment, node, target) / steps;
    }

    /**
     * @param environment
     *            the environment
     * @param currentNode
     *            the node
     * @param targetPosition
     *            the target
     * @return an estimation of the distance between the node and the target
     *         position
     */
    protected abstract double computeDistance(
        MapEnvironment<T, O, S> environment,
        Node<T> currentNode,
        GeoPosition targetPosition
    );

    /**
     *
     * @return the environment
     */
    protected MapEnvironment<T, O, S> getEnvironment() {
        return environment;
    }
}
