package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * An alias for a group of pedestrians.
 */
interface PedestrianGroup<T, P : Vector<P>, A : GeometricTransformation<P>> : Group<T, Pedestrian<T, P, A>>
