package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Implementation of a generic [SteeringActionWithTarget] in a vector space.
 *
 * @param environment
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
open class SteeringActionWithTargetImpl<T, P, A> @JvmOverloads constructor(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    protected val pedestrian: Pedestrian<T, P, A>,
    protected val target: TargetSelectionStrategy<P>,
    protected val speed: SpeedSelectionStrategy<P> = SpeedSelectionStrategy { pedestrian.speed() / reaction.rate },
    protected val routing: RoutingStrategy<P> = RoutingStrategy { p1, p2 -> PolygonalChain(p1, p2) }
) : AbstractConfigurableMoveNode<T, P>(
    environment,
    pedestrian,
    routing,
    target,
    speed
), SteeringActionWithTarget<T, P>
    where
        A : GeometricTransformation<P>,
        P : Position<P>,
        P : Vector<P> {

    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        requireNodeTypeAndProduce<Pedestrian<T, P, A>, SteeringActionWithTargetImpl<T, P, A>>(n) {
            SteeringActionWithTargetImpl(environment, r, it, target, speed, routing)
        }

    /**
     * Next relative position.
     */
    override fun interpolatePositions(
        current: P,
        target: P,
        maxWalk: Double
    ): P = when {
        current.distanceTo(target) <= maxWalk -> target - current
        else -> (target - current).resized(maxWalk)
    }

    override fun nextPosition(): P = nextPosition

    override fun target(): P = target.target

    override fun getNode(): Pedestrian<T, P, A> = pedestrian

    protected inline fun <reified N : Node<*>, S : SteeringActionWithTargetImpl<T, P, A>> requireNodeTypeAndProduce(
        node: Node<*>,
        builder: (N) -> S
    ): S {
        require(node is N) { "Incompatible node type. Required ${N::class}, found ${node::class}" }
        return builder(node)
    }
}
