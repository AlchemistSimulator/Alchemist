package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.direction
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.implementations.utils.position
import it.unibo.alchemist.model.implementations.utils.shuffled
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.random.RandomGenerator

/**
 * Give the impression of a random walk through the environment targeting an ever changing pseudo-randomly point
 * of a circumference at a given distance and with a given radius from the current pedestrian position.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param offset
 *          the distance from the pedestrian position of the center of the circle.
 * @param radius
 *          the radius of the circle.
 */
open class Wander<T>(
    private val env: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian<T>,
    private val rg: RandomGenerator,
    private val offset: Double,
    private val radius: Double
) : SteeringActionImpl<T, Euclidean2DPosition>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { rg.position() }
) {

    private val heading by lazy {
        env.setHeading(pedestrian, rg.direction()).let {
            { env.getHeading(pedestrian) }
        }
    }

    override fun interpolatePositions(current: Euclidean2DPosition, target: Euclidean2DPosition, maxWalk: Double) =
        super.interpolatePositions(
            env.origin(),
            heading().asAngle
                .let { Euclidean2DPosition(offset * cos(it), offset * sin(it)) }
                .let { it.surrounding(env, radius).shuffled(rg).first() },
            maxWalk
        )
}
