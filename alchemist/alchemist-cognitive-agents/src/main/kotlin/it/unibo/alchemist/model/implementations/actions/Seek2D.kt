package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.EuclideanEnvironment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * [Seek] behavior in a bidimensional environment. This action is more sophisticated than [Seek] and tries to avoid
 * other agents while moving.
 * This behavior is restricted to two dimensions because some geometry utils available only in 2D are required to
 * implement it.
 */
open class Seek2D<T, P, A>(
    /**
     * The environment the pedestrian is into.
     */
    protected val environment: EuclideanEnvironment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    target: P
) : Seek<T, P, A>(environment, reaction, pedestrian, target)
    where P : Position2D<P>, P : Vector2D<P>,
          A : GeometricTransformation<P> {

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
            Seek2D(environment, r, it, target)
        }
}
