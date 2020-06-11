package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.EuclideanEnvironment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * [Seek] behavior in a bidimensional environment. The actions performed are more
 * sophisticated and allow the pedestrian to try to avoid other agents on its path.
 * This behavior is restricted to two dimensions because some geometry utils available
 * only in 2D are required to implement it.
 */
open class Seek2D<T, P, A>(
    protected val environment: EuclideanEnvironment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    coordinates: P
) : Seek<T, P, A>(environment, reaction, pedestrian, coordinates)
    where
        A : GeometricTransformation<P>,
        P : Position2D<P>,
        P : Vector2D<P> {

    constructor(
        environment: EuclideanEnvironment<T, P>,
        reaction: Reaction<T>,
        pedestrian: Pedestrian<T, P, A>,
        x: Number,
        y: Number
    ) : this(environment, reaction, pedestrian, environment.makePosition(x, y))

    public override fun interpolatePositions(current: P, target: P, maxWalk: Double): P {
        val superPosition = current + super.interpolatePositions(current, target, maxWalk)
        return (current.surrounding(maxWalk) + superPosition)
            .asSequence()
            .discardUnsuitablePositions(environment, pedestrian)
            .minBy { it.distanceTo(target) }?.minus(current)
            ?: environment.origin
    }

    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        requireNodeTypeAndProduce<Pedestrian<T, P, A>, Seek2D<T, P, A>>(n) {
            Seek2D(environment, r, it, coordinates)
        }
}
