package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

open class Arrive<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    decelerationRadius: Double, // the distance from which the pedestrian starts to decelerate
    arrivalTolerance: Double, // the distance at which the pedestrian is considered arrived to the current target
    vararg coords: Double
) : SteeringActionImpl<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy { env.makePosition(*coords.toTypedArray()) },
    SpeedSelectionStrategy {
        target -> with(env.getPosition(pedestrian).getDistanceTo(target)) {
            when {
                this < arrivalTolerance -> 0.0
                this < decelerationRadius -> pedestrian.walkingSpeed * this / decelerationRadius
                else -> pedestrian.walkingSpeed
            }
        }
    }
)