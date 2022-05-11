/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.properties.Physical2D
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.PhysicalProperty
import org.dyn4j.dynamics.PhysicsBody
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World
import java.awt.Color

private typealias PhysicalProperty2D<T> =
    PhysicalProperty<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>

class EnvironmentWithDynamics<T>(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    path: String,
    zoom: Double = 1.0,
    dx: Double = 0.0,
    dy: Double = 0.0,
    obstaclesColor: Int = Color.BLACK.rgb,
    roomsColor: Int = Color.BLUE.rgb
) : Dynamics2DEnvironment<T>,
    Physics2DEnvironment<T> by ImageEnvironmentWithGraph(
        incarnation,
        path,
        zoom,
        dx,
        dy,
        obstaclesColor,
        roomsColor,
    ) {

    private val world: World<PhysicsBody> = World()

    init {
        world.gravity = Vector2(0.0, 0.0)
    }

    override fun addNode(node: Node<T>, position: Euclidean2DPosition) {
        val nodePhysics = node.asPropertyOrNull<T, PhysicalProperty2D<T>>()
        require(nodePhysics != null && nodePhysics is PhysicsBody) {
            "This environments require that all nodes have physical property " +
                "and in particular are a kind of ${PhysicsBody::class.simpleName}"
        }
        nodePhysics.translateToOrigin()
        nodePhysics.translate(position.x, position.y)
        world.addBody(nodePhysics)
    }

    override fun updatePhysics(elapsedTime: Double) {
        world.update(elapsedTime)
    }

    override fun moveNodeToPosition(node: Node<T>, position: Euclidean2DPosition) {
        getNodePhysics(node).transform = Transform().apply {
            translate(position.x, position.y)
        }
    }

    override fun getPosition(node: Node<T>): Euclidean2DPosition = getNodePhysics(node).position()

    private fun getNodePhysics(node: Node<T>) = world
        .bodies
        .asSequence()
        .filterIsInstance(Physical2D::class.java)
        .find { it.node == node } ?: throw IllegalArgumentException("$node was not found in the current environment")

    private fun Physical2D<*>.position() =
        Euclidean2DPosition(this.transform.translationX, this.transform.translationY)
}
