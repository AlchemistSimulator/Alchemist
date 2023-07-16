/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps;

import it.unibo.alchemist.model.BenchmarkableEnvironment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Route;
import it.unibo.alchemist.model.RoutingService;
import it.unibo.alchemist.model.RoutingServiceOptions;

/**
 * @param <T> Concentration type
 * @param <O> {@link RoutingServiceOptions} type
 * @param <S> {@link RoutingService} type
 */
public interface MapEnvironment<T, O extends RoutingServiceOptions<O>, S extends RoutingService<GeoPosition, O>>
        extends BenchmarkableEnvironment<T, GeoPosition> {

    /**
     * This method relies on the map data, and computes a route towards some
     * absolute coordinate solving a TSP problem. It's up to the specific
     * {@link it.unibo.alchemist.model.Action} calling this method to effectively move nodes along the
     * path. It uses the fastest path as metric.
     * 
     * @param node
     *            The start node
     * @param node2
     *            the second node's position will be used as destination
     * @return A {@link Route} object describing the path the node should
     *         follow
     */
    Route<GeoPosition> computeRoute(Node<T> node, Node<T> node2);

    /**
     * This method relies on the map data, and computes a route towards some
     * absolute coordinate solving a TSP problem. It's up to the specific
     * {@link it.unibo.alchemist.model.Action} calling this method to effectively move nodes along the
     * path.
     * 
     * @param node
     *            The {@link Node} to move
     * @param coord
     *            The absolute coordinate where this node wants to move to
     * @return A {@link Route} object describing the path the node should
     *         follow
     */
    Route<GeoPosition> computeRoute(Node<T> node, GeoPosition coord);

    /**
     * This method relies on the map data, and computes a route towards some
     * absolute coordinate solving a TSP problem. It's up to the specific
     * {@link it.unibo.alchemist.model.Action} calling this method to effectively move nodes along the
     * path.
     * 
     * @param node
     *            The {@link Node} to move
     * @param coord
     *            The absolute coordinate where this node wants to move to
     * @param options
     *            The options tipe for this route
     * @return A {@link Route} object describing the path the node should
     *         follow
     */
    Route<GeoPosition> computeRoute(Node<T> node, GeoPosition coord, O options);

    /**
     * This method relies on the map data, and computes a route towards some
     * absolute coordinate solving a TSP problem. It's up to the specific
     * {@link it.unibo.alchemist.model.Action} calling this method to effectively move nodes along the
     * path.
     * 
     * @param p1
     *            start position
     * 
     * @param p2
     *            end position The absolute coordinate where this node wants to
     *            move to
     * @return A {@link Route} object describing the path the node should
     *         follow
     */
    Route<GeoPosition> computeRoute(GeoPosition p1, GeoPosition p2);

    /**
     * This method relies on the map data, and computes a route towards some
     * absolute coordinate solving a TSP problem. It's up to the specific
     * {@link it.unibo.alchemist.model.Action} calling this method to effectively move nodes along the
     * path.
     * 
     * @param from
     *            start position
     * 
     * @param to
     *            end position The absolute coordinate where this node wants to
     *            move to
     * @param options
     *            options to use. Different vehicles may use different paths,
     *            e.g. pedestrians can't go along a highway, but can walk the
     *            parks
     * @return A {@link Route} object describing the path the node should
     *         follow
     */
    Route<GeoPosition> computeRoute(GeoPosition from, GeoPosition to, O options);

    /**
     * @return the {@link RoutingService} for this environment
     */
    S getRoutingService();

}
