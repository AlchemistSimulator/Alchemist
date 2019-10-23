package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.implementations.utils.makePosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Move the agent towards a target position.
 * It is similar to Seek but it attempts to arrive at the target position with a zero velocity.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param decelerationRadius
 *          the distance from which the pedestrian starts to decelerate.
 * @param arrivalTolerance
 *          the distance at which the pedestrian is considered arrived to the target.
 * @param coords
 *          the coordinates of the position the pedestrian moves towards.
 */
open class Arrive<T, P : Position<P>>(
    env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>,
    decelerationRadius: Double,
    arrivalTolerance: Double,
    vararg coords: Double
) : SteeringActionImpl<T, P>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { env.makePosition(coords.toTypedArray()) },
    SpeedSelectionStrategy {
        target -> with(env.getPosition(pedestrian).getDistanceTo(target)) {
            when {
                this < arrivalTolerance -> 0.0
                this < decelerationRadius -> Speed.default * this / decelerationRadius
                else -> pedestrian.speed()
            }
        }
    }
)
