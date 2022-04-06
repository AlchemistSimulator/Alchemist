package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.model.util.RandomGeneratorExtension.nextDouble
import it.unibo.alchemist.model.util.RandomGeneratorExtension.shuffled
import org.apache.commons.math3.random.RandomGenerator

/**
 * Give the impression of a random walk through the environment targeting an ever changing pseudo-randomly point
 * of a circumference at a given distance and with a given radius from the current node position.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param node
 *          the owner of this action.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 * @param offset
 *          the distance from the node position of the center of the circle.
 * @param radius
 *          the radius of the circle.
 */
open class CognitiveAgentWander<T>(
    private val environment: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    node: Node<T>,
    protected val randomGenerator: RandomGenerator,
    protected val offset: Double,
    protected val radius: Double,
) : AbstractSteeringActionWithTarget<T, Euclidean2DPosition, Euclidean2DTransformation>(
    environment,
    reaction,
    node,
    TargetSelectionStrategy { randomGenerator.position(environment) }
) {

    private val heading by lazy {
        environment.setHeading(node, randomGenerator.random2DVersor(environment)).let {
            { environment.getHeading(node) }
        }
    }

    override fun nextPosition(): Euclidean2DPosition = heading()
        .resized(offset)
        .surrounding(radius)
        .shuffled(randomGenerator)
        .first()
        .coerceAtMost(maxWalk)

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        CognitiveAgentWander(environment, reaction, node, randomGenerator, offset, radius)
}

/**
 * Generate a random Euclidean position.
 */
private fun RandomGenerator.position(environment: Environment<*, Euclidean2DPosition>) =
    random2DVersor(environment).let {
        val distance = nextInt()
        Euclidean2DPosition(it.x * distance, it.y * distance)
    }

/**
 * Generate a random Euclidean direction.
 */
private fun <V> RandomGenerator.random2DVersor(environment: Environment<*, V>): V
    where V : Vector2D<V>, V : Position2D<V> =
    environment.makePosition(nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0))
