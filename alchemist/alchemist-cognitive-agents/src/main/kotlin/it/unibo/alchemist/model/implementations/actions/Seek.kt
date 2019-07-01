package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

open class Seek<T, P : Position2D<P>>(
    private val env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    x: Double,
    y: Double
) : AbstractConfigurableMoveNode<T, P>(
    env,
    pedestrian,
    RoutingStrategy { p1, p2 -> PolygonalChain(p1, p2) },
    TargetSelectionStrategy { env.makePosition(x, y) },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
) {
    override fun cloneAction(n: Node<T>?, r: Reaction<T>?) = TODO()

    override fun getDestination(current: P, target: P, maxWalk: Double): P =
        with(current.getDistanceTo(target) / maxWalk) {
            env.makePosition(*(target - current).cartesianCoordinates.map { it / this }.toTypedArray())
        }
}
