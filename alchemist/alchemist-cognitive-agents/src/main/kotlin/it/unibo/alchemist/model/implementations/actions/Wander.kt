package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.utils.makePosition
import it.unibo.alchemist.model.implementations.actions.utils.nextDouble
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import org.apache.commons.math3.random.RandomGenerator

/**
 * Give the impression of a random walk through the environment.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param radius
 *          the radius of the circle with center in current pedestrian position
 *          and inside which the target position is pseudo-randomly determined.
 */
open class Wander<T, P : Position<P>>(
    private val env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    rg: RandomGenerator,
    radius: Double
) : SteeringActionImpl<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy { with(env) {
        getPosition(pedestrian) + makePosition(
            (1..dimensions).map { rg.nextDouble(-1.0, 1.0) * radius }.toTypedArray()
        )
    } },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
)