/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.obstacles.RectObstacle2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.PhysicsUpdate
import it.unibo.alchemist.model.interfaces.GlobalReaction
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.PhysicsBody
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World
import java.awt.Color

private typealias PhysicsEnvironmentWithObstacles<T> =
    EuclideanPhysics2DEnvironmentWithObstacles<RectObstacle2D<Euclidean2DPosition>, T>

/**
 * This Environment uses hooks provided by [Dynamics2DEnvironment] to update
 * the physical world, It also applies physical properties to any added node to
 * perform collision detection and response.
 * If an image path is provided a backing [ImageEnvironmentWithGraph] is used, otherwise
 * the [Continuous2DEnvironment] will be used.
 */
class EnvironmentWithDynamics<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    path: String? = null,
    zoom: Double = 1.0,
    dx: Double = 0.0,
    dy: Double = 0.0,
    obstaclesColor: Int = Color.BLACK.rgb,
    roomsColor: Int = Color.BLUE.rgb,
    private val backingEnvironment: Physics2DEnvironment<T> = path?.let {
        ImageEnvironmentWithGraph(incarnation, it, zoom, dx, dy, obstaclesColor, roomsColor)
    } ?: Continuous2DEnvironment(incarnation),
) : Dynamics2DEnvironment<T>,
    PhysicsEnvironmentWithObstacles<T> by backingEnvironment.asEnvironmentWithObstacles() {

    private val world: World<PhysicsBody> = World()

    private val nodeToBody: MutableMap<Node<T>, PhysicsBody> = mutableMapOf()

    private var physicsUpdate = PhysicsUpdate(this, 1.0)

    private var physicsUpdateHasBeenOverriden: Boolean

    init {
        world.gravity = World.ZERO_GRAVITY
        /*
         * This flag is defaulted to true. The engine automatically detects
         * whether a node body stops (its linear velocity is below a certain threshold)
         * and eventually puts it at rest. This means tha in future world.update() calls,
         * the at-rest node will not be considered for the environment update.
         * We do not want this because we always need no move each node, even if the movement is mimimal,
         * in order to progress their cognitive perception for example.
         *
         * For further references: https://dyn4j.org/pages/advanced.html
         */
        world.settings.isAtRestDetectionEnabled = false
        addGlobalReaction(physicsUpdate)
        physicsUpdateHasBeenOverriden = false
        obstacles.forEach { obstacle ->
            addObstacleToWorld(obstacle)
        }
    }

    override fun addGlobalReaction(reaction: GlobalReaction<T>) {
        if (reaction is PhysicsUpdate) {
            require(!physicsUpdateHasBeenOverriden) {
                "${PhysicsUpdate::class.simpleName} reaction had been already overriden"
            }
            removeGlobalReaction(physicsUpdate)
            physicsUpdateHasBeenOverriden = true
            physicsUpdate = reaction
        }
        backingEnvironment.addGlobalReaction(reaction)
    }

    private fun addObstacleToWorld(obstacle: RectObstacle2D<Euclidean2DPosition>) {
        val obstacleBody = Body()
        obstacleBody.addFixture(Rectangle(obstacle.width, obstacle.height))
        obstacleBody.setMass(MassType.INFINITE)
        obstacleBody.transform = Transform().apply {
            translate(obstacle.center)
        }
        world.addBody(obstacleBody)
    }

    private val RectObstacle2D<Euclidean2DPosition>.center get() =
        Vector2(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2)

    override fun addNode(node: Node<T>, position: Euclidean2DPosition): Boolean {
        if (backingEnvironment.addNode(node, position)) {
            addNodeBody(node)
            moveNodeBodyToPosition(node, position)
            return true
        }
        return false
    }

    private fun addNodeBody(node: Node<T>) {
        val nodeBody = Body()
        addPhysicalProperties(nodeBody, node.asProperty<T, AreaProperty<T>>().shape.radius)
        nodeToBody[node] = nodeBody
        world.addBody(nodeBody)
        node.asProperty<T, PhysicalPedestrian2D<T>>()
            .onFall {
                /*
                 * This disables collision response with the falling agent, as
                 * overlapping is allowed in this case.
                 * Agents approaching a falling agent will be subject to a
                 * proper avoidance force. see the PhysicalPedestrian interface.
                 */
                nodeToBody[it]?.isEnabled = false
            }
    }

    private fun moveNodeBodyToPosition(node: Node<T>, position: Euclidean2DPosition) {
        nodeToBody[node]?.transform = Transform().apply {
            translate(position.x, position.y)
        }
    }

    override fun moveNode(node: Node<T>, direction: Euclidean2DPosition) {
        backingEnvironment.moveNode(node, direction)
        moveNodeBodyToPosition(node, backingEnvironment.getPosition(node))
    }

    private fun addPhysicalProperties(body: PhysicsBody, radius: Double) {
        body.addFixture(Circle(radius))
        body.setMass(MassType.NORMAL)
    }

    override fun setVelocity(node: Node<T>, velocity: Euclidean2DPosition) =
        nodeToBody[node]?.let { it.linearVelocity = Vector2(velocity.x, velocity.y) }
            ?: error("Unable to update $node physical state. Check if it was added to this environment.")

    override fun getVelocity(node: Node<T>) = nodeToBody[node]?.let {
        Euclidean2DPosition(it.linearVelocity.x, it.linearVelocity.y)
    } ?: this.origin

    override fun updatePhysics(elapsedTime: Double) {
        world.update(elapsedTime, Int.MAX_VALUE)
        /*
         * Make world and environment position consistent
         */
        nodeToBody.forEach { (node, body) ->
            moveNodeToPosition(node, body.position)
        }
    }

    override fun getPosition(node: Node<T>): Euclidean2DPosition = nodeToBody[node]?.position
        ?: throw IllegalArgumentException("Unable to find $node's position in the environment.")

    private val PhysicsBody.position get() =
        Euclidean2DPosition(this.transform.translationX, this.transform.translationY)

    override val origin: Euclidean2DPosition get() = backingEnvironment.origin

    override fun makePosition(vararg coordinates: Double): Euclidean2DPosition =
        backingEnvironment.makePosition(*coordinates)

    companion object {

        private fun <T> Physics2DEnvironment<T>.asEnvironmentWithObstacles(): PhysicsEnvironmentWithObstacles<T> =
            if (this is EuclideanPhysics2DEnvironmentWithObstacles<*, T>) {
                @Suppress("UNCHECKED_CAST")
                this as PhysicsEnvironmentWithObstacles<T>
            } else {
                object : Physics2DEnvironment<T> by this, PhysicsEnvironmentWithObstacles<T> {
                    override fun getObstaclesInRange(
                        center: Euclidean2DPosition,
                        range: Double,
                    ): List<RectObstacle2D<Euclidean2DPosition>> = emptyList()

                    override fun getObstaclesInRange(
                        centerx: Double,
                        centery: Double,
                        range: Double,
                    ): List<RectObstacle2D<Euclidean2DPosition>> = emptyList()

                    override fun hasMobileObstacles() = false

                    override val obstacles: List<RectObstacle2D<Euclidean2DPosition>>
                        get() = emptyList()

                    override fun intersectsObstacle(start: Euclidean2DPosition, end: Euclidean2DPosition) = false

                    override fun next(current: Euclidean2DPosition, desired: Euclidean2DPosition) = desired

                    override fun removeObstacle(obstacle: RectObstacle2D<Euclidean2DPosition>) =
                        error("This Environment instance does not support obstacle removal")

                    override fun addObstacle(obstacle: RectObstacle2D<Euclidean2DPosition>) =
                        error("This Environment instance does not support adding obstacles")

                    override fun makePosition(vararg coordinates: Double) =
                        this@asEnvironmentWithObstacles.makePosition(*coordinates)

                    override val origin
                        get() = this@asEnvironmentWithObstacles.origin
                }
            }
    }
}
