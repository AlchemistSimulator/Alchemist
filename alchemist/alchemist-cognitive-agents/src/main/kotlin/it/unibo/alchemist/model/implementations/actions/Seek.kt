package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Move the pedestrian towards the target position as fast as possible.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param target
 *          the position the pedestrian moves towards.
 */
open class Seek<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    target: P
) : Arrive<T, P, A>(environment, reaction, pedestrian, 0.0, 0.0, target)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    constructor(
        environment: Environment<T, P>,
        reaction: Reaction<T>,
        pedestrian: Pedestrian<T, P, A>,
        vararg coordinates: Number
    ) : this(environment, reaction, pedestrian, environment.makePosition(*coordinates))

    override fun cloneAction(n: Pedestrian<T, P, A>, r: Reaction<T>) = Seek(environment, r, n, target)
}
