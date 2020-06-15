package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
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
 * A [SteeringActionWithTarget] in a vector space.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param targetSelection
 *          the strategy used to compute the next target.
 * @param speed
 *          the speed selection strategy.
 * @param routing
 *          the routing strategy.
 */
abstract class AbstractSteeringActionWithTarget<T, P, A> @JvmOverloads constructor(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    protected val pedestrian: Pedestrian<T, P, A>,
    protected val targetSelection: TargetSelectionStrategy<P>,
    protected val speed: SpeedSelectionStrategy<P> = SpeedSelectionStrategy { pedestrian.speed() / reaction.rate },
    protected val routing: RoutingStrategy<P> = RoutingStrategy { p1, p2 -> PolygonalChain(p1, p2) }
) : AbstractEuclideanConfigurableMoveNode<T, P>(
    environment,
    pedestrian,
    routing,
    targetSelection,
    speed
), SteeringActionWithTarget<T, P>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    override fun nextPosition(): P = nextPosition

    override fun target(): P = targetSelection.target

    override fun getNode(): Pedestrian<T, P, A> = pedestrian
}
