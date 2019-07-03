package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

open class Arrive<T, P : Position<P>>(
    private val env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    decelerationRadius: Double, // the distance from which the pedestrian starts to decelerate
    arrivalTolerance: Double, // the distance at which the pedestrian is considered arrived to the current target
    vararg coords: Double
) : AbstractConfigurableMoveNode<T, P>(
    env,
    pedestrian,
    RoutingStrategy { p1, p2 -> PolygonalChain(p1, p2) },
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
) {
    override fun cloneAction(n: Node<T>?, r: Reaction<T>?) = TODO()

    override fun getDestination(current: P, target: P, maxWalk: Double): P =
        if (maxWalk > 0)
            env.makePosition(*(target - current).cartesianCoordinates.map { it / (current.getDistanceTo(target) / maxWalk) }.toTypedArray())
        else
            target
}