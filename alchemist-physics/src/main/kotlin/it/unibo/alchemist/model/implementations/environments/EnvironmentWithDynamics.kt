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
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import it.unibo.alchemist.model.interfaces.properties.PhysicalProperty
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.PhysicsBody
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World
import java.awt.Color

typealias PhysicalProperty2D<T> =
    PhysicalProperty<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>

class EnvironmentWithDynamics<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    path: String? = null,
    zoom: Double = 1.0,
    dx: Double = 0.0,
    dy: Double = 0.0,
    obstaclesColor: Int = Color.BLACK.rgb,
    roomsColor: Int = Color.BLUE.rgb,
    private val backingEnvironment: Physics2DEnvironment<T> = path?.let {
        ImageEnvironmentWithGraph(
            incarnation,
            it,
            zoom,
            dx,
            dy,
            obstaclesColor,
            roomsColor,
        )
    } ?: Continuous2DEnvironment(incarnation),
) : Dynamics2DEnvironment<T>,
    Physics2DEnvironment<T> by backingEnvironment {

    private val world: World<PhysicsBody> = World()

    private val nodeToBody: MutableMap<Node<T>, PhysicsBody> = mutableMapOf()

    init {
        world.gravity = Vector2(0.0, 0.0)
    }

    override fun addNode(node: Node<T>, position: Euclidean2DPosition) {
        backingEnvironment.addNode(node, position)
        addNodeBody(node)
        moveNodeBodyToPosition(node, position)
    }

    private fun addNodeBody(node: Node<T>) {
        val nodeBody = Body()
        addPhysicalProperties(nodeBody, node.asProperty<T, AreaProperty<T>>().shape.radius)
        nodeToBody[node] = nodeBody
        world.addBody(nodeBody)
    }
    private fun moveNodeBodyToPosition(node: Node<T>, position: Euclidean2DPosition) {
        nodeToBody[node]?.transform = Transform().apply {
            translate(position.x, position.y)
        }
    }

    private fun addPhysicalProperties(body: PhysicsBody, radius: Double) {
        body.addFixture(Circle(radius))
        body.setMass(MassType.NORMAL)
    }

    override fun setVelocity(node: Node<T>, velocity: Euclidean2DPosition) {
        moveNodeToPosition(node, nodeToBody[node]?.position)
        nodeToBody[node]?.linearVelocity = Vector2(velocity.x, velocity.y)
    }

    override fun updatePhysics(elapsedTime: Double) {
        world.update(elapsedTime)
    }

    override fun getPosition(node: Node<T>): Euclidean2DPosition = nodeToBody[node]?.position
        ?: throw IllegalArgumentException("Unable to find $node's position in the environment.")

    private val PhysicsBody.position get() =
        Euclidean2DPosition(this.transform.translationX, this.transform.translationY)

    override val origin: Euclidean2DPosition get() = backingEnvironment.origin

    override fun makePosition(vararg coordinates: Double): Euclidean2DPosition =
        backingEnvironment.makePosition(*coordinates)
}
