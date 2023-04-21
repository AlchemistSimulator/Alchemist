package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.loader.deployments.Polygon
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.implementations.movestrategies.ChangeTargetOnCollision
import it.unibo.alchemist.model.implementations.movestrategies.speed.ConstantSpeed
import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.MapEnvironment
import it.unibo.alchemist.model.interfaces.RoutingService
import it.unibo.alchemist.model.interfaces.RoutingServiceOptions
import org.apache.commons.math3.random.RandomGenerator
import java.lang.IllegalStateException

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
