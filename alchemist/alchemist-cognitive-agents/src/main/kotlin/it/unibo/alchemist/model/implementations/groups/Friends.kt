package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A generic, leaderless group of pedestrians.
 */
class Friends<T, S : Vector<S>, A : GeometricTransformation<S>> : GenericGroup<T, Pedestrian<T, S, A>>()
