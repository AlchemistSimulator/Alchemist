package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import kotlin.math.cos
import kotlin.math.sin

/**
 * It's an [AbstractConfigurableMoveNode] for [Euclidean2DPosition] which provides a default [getDestination] that is
 * accurate in regards to the target given and the current maximum speed.
 */
abstract class AbstractConfigurableMoveNodeWithAccurateEuclideanDestination<T>(
    environment: Environment<T, Euclidean2DPosition>,
    node: Node<T>,
    routing: RoutingStrategy<Euclidean2DPosition>,
    target: TargetSelectionStrategy<Euclidean2DPosition>,
    speed: SpeedSelectionStrategy<Euclidean2DPosition>
) : AbstractConfigurableMoveNode<T, Euclidean2DPosition>(environment, node, routing, target, speed) {
    /**
     * If [maxWalk] is greater than the speed needed to reach [target] then it positions precisely on [target] without going
     * farther.
     */
    override fun getDestination(current: Euclidean2DPosition, target: Euclidean2DPosition, maxWalk: Double): Euclidean2DPosition =
        with(target - current) {
            if (getDistanceTo(current) < maxWalk) {
                this
            } else {
                val angle = this.asAngle()
                environment.makePosition(maxWalk * cos(angle), maxWalk * sin(angle))
            }
        }
}