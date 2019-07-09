package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

open class Seek<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    vararg coords: Double
) : Arrive<T, P>(env, pedestrian, 0.0, 0.0, *coords)