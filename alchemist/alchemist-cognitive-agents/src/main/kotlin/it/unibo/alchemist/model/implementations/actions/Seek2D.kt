package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.EuclideanEnvironment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * [Seek] behavior in a bidimensional environment, delegated to [FollowScalarField] (this means the pedestrian tries
 * to overtake others on its path, in general its movements are more sophisticated than [Seek]).
 */
open class Seek2D<T, P, A>(
    /**
     * The environment the pedestrian is into.
     */
    protected val environment: EuclideanEnvironment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    /**
     * The position the pedestrian wants to reach.
     */
    private val target: P
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian),
    SteeringActionWithTarget<T, P>
    where P : Position2D<P>, P : Vector2D<P>,
          A : GeometricTransformation<P> {

    constructor(
        environment: EuclideanEnvironment<T, P>,
        reaction: Reaction<T>,
        pedestrian: Pedestrian<T, P, A>,
        x: Number,
        y: Number
    ) : this(environment, reaction, pedestrian, environment.makePosition(x, y))

    private val followScalarField = FollowScalarField(environment, reaction, pedestrian, target) {
        -it.distanceTo(target)
    }

    override fun target(): P = target

    override fun nextPosition(): P = followScalarField.nextPosition()

    override fun cloneAction(n: Pedestrian<T, P, A>, r: Reaction<T>) = Seek2D(environment, r, n, target)
}
