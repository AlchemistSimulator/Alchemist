/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions;

import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.RoutingService;
import it.unibo.alchemist.model.RoutingServiceOptions;
import it.unibo.alchemist.model.maps.GPSTrace;
import it.unibo.alchemist.model.maps.MapEnvironment;
import it.unibo.alchemist.model.maps.movestrategies.routing.OnStreets;
import it.unibo.alchemist.model.maps.movestrategies.speed.RoutingTraceDependantSpeed;
import it.unibo.alchemist.model.maps.movestrategies.target.FollowTrace;
import it.unibo.alchemist.model.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy;

import java.io.Serial;

/**
 * A walker that follows a trace. The trace is mandatory.
 *
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
 */
public final class GPSTraceWalker<T, O extends RoutingServiceOptions<O>, S extends RoutingService<GeoPosition, O>>
    extends MoveOnMapWithGPS<T, O, S> {

    @Serial
    private static final long serialVersionUID = -6495138719085165782L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param options
     *            options for the computation of routes
     * @param path
     *            resource (file, directory, ...) with GPS trace
     * @param cycle
     *            true if the traces have to be distributed cyclically
     * @param normalizer
     *            name of the class that implement the strategy to normalize the
     *            time
     * @param normalizerArgs
     *            Args to build the normalizer
     */
    public GPSTraceWalker(
            final MapEnvironment<T, O, S> environment,
            final Node<T> node,
            final Reaction<T> reaction,
            final O options,
            final String path,
            final boolean cycle,
            final String normalizer,
            final Object... normalizerArgs
    ) {
        this(
            environment,
            node,
            reaction,
            options,
            traceFor(environment, path, cycle, normalizer, normalizerArgs)
        );
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param path
     *            resource (file, directory, ...) with GPS trace
     * @param cycle
     *            true if the traces have to be distributed cyclically
     * @param normalizer
     *            name of the class that implement the strategy to normalize the
     *            time
     * @param normalizerArgs
     *            Args to build the normalizer
     */
    public GPSTraceWalker(
        final MapEnvironment<T, O, S> environment,
        final Node<T> node,
        final Reaction<T> reaction,
        final String path,
        final boolean cycle,
        final String normalizer,
        final Object... normalizerArgs
    ) {
        this(
            environment,
            node,
            reaction,
            environment.getRoutingService().getDefaultOptions(),
            path,
            cycle,
            normalizer,
            normalizerArgs
        );
    }

    private GPSTraceWalker(
        final MapEnvironment<T, O, S> environment,
        final Node<T> node,
        final Reaction<T> reaction,
        final O options,
        final GPSTrace trace
    ) {
        this(
            environment,
            node,
            new OnStreets<>(environment, options),
            new RoutingTraceDependantSpeed<>(environment, node, reaction, options),
            new FollowTrace<>(reaction),
            trace
        );
    }

    private GPSTraceWalker(
            final MapEnvironment<T, O, S> environment,
            final Node<T> node,
            final RoutingStrategy<T, GeoPosition> routingStrategy,
            final SpeedSelectionStrategy<T, GeoPosition> speedSelectionStrategy,
            final TargetSelectionStrategy<T, GeoPosition> targetSelectionStrategy,
            final GPSTrace trace
    ) {
        super(
            environment,
            node,
            routingStrategy,
            speedSelectionStrategy,
            targetSelectionStrategy,
            trace
        );
    }

    @Override
    public GPSTraceWalker<T, O, S> cloneAction(final Node<T> node, final Reaction<T> reaction) {
        return new GPSTraceWalker<>(
            getEnvironment(),
            node,
            getRoutingStrategy().cloneIfNeeded(node, reaction),
            getSpeedSelectionStrategy().cloneIfNeeded(node, reaction),
            getTargetSelectionStrategy().cloneIfNeeded(node, reaction),
            getTrace()
        );
    }

}
