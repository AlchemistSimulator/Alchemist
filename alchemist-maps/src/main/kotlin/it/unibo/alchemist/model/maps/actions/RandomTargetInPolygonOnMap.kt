/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.RoutingService
import it.unibo.alchemist.model.RoutingServiceOptions
import it.unibo.alchemist.model.deployments.Polygon
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.movestrategies.ChangeTargetOnCollision
import it.unibo.alchemist.model.movestrategies.speed.ConstantSpeed
import it.unibo.alchemist.model.routes.PolygonalChain
import org.apache.commons.math3.random.RandomGenerator

/**
 * This actions generates random waypoints inside a Polygon.
 * The polygon can be provided either through as a deployment ([positionGenerator]), or as `List<List<Number>>`
 * (`polygonCoordinates`)
 */
class RandomTargetInPolygonOnMap<T, O : RoutingServiceOptions<O>, S : RoutingService<GeoPosition, O>>(
    environment: MapEnvironment<T, O, S>,
    node: Node<T>,
    reaction: Reaction<T>,
    speed: Double,
    val positionGenerator: Polygon<GeoPosition>,
) : MoveOnMap<T, O, S>(
    environment,
    node,
    { current, final -> PolygonalChain(current, final) },
    ConstantSpeed(reaction, speed),
    object : ChangeTargetOnCollision<T, GeoPosition>({ environment.getPosition(node) }) {
        override fun chooseTarget() = positionGenerator.stream()
            .findFirst()
            .orElseThrow { IllegalStateException("Bug in Alchemist.") }

        override fun cloneIfNeeded(destination: Node<T>?, reaction: Reaction<T>?) = this
    },
) {
    constructor(
        randomGenerator: RandomGenerator,
        environment: MapEnvironment<T, O, S>,
        node: Node<T>,
        reaction: Reaction<T>,
        speed: Double,
        polygonCoordinates: List<List<Number>>,
    ) : this (environment, node, reaction, speed, Polygon(environment, randomGenerator, 1, polygonCoordinates))
}
