package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction

/**
 * Move the pedestrian towards the target position as fast as possible.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param coords
 *          the coordinates of the position the pedestrian moves towards.
 */
open class Seek<T, P : Position<P>>(
    env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Arrive<T, P>(env, reaction, pedestrian, 0.0, 0.0, *coords)