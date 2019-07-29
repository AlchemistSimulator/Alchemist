package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.utils.div
import it.unibo.alchemist.model.implementations.actions.utils.makePosition
import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Generic implementation of an action adhering the SteeringAction interface.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param target
 *          the strategy used to compute the next target.
 * @param speed
 *          the speed selection strategy.
 * @param routing
 *          the routing strategy.
 */
open class SteeringActionImpl<T, P : Position<P>> @JvmOverloads constructor(
    private val env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    target: TargetSelectionStrategy<P>,
    speed: SpeedSelectionStrategy<P>,
    routing: RoutingStrategy<P> = RoutingStrategy { p1, p2 -> PolygonalChain(p1, p2) }
) : AbstractConfigurableMoveNode<T, P>(env, pedestrian, routing, target, speed), SteeringAction<T, P> {

    override fun cloneAction(n: Node<T>?, r: Reaction<T>?) = TODO()

    override fun getDestination(current: P, target: P, maxWalk: Double): P = with(current.getDistanceTo(target)) {
        if (this < maxWalk)
            target
        else
            env.makePosition((target - current) / (this / maxWalk))
    }

    override fun nextPosition(): P = nextPosition

    override fun target(): P = targetPoint
}