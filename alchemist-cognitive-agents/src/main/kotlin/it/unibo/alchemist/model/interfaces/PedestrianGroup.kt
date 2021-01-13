package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A [Group] of [Pedestrian]s.
 */
interface PedestrianGroup<T, P : Vector<P>, A : GeometricTransformation<P>> : Group<T, Pedestrian<T, P, A>>

/**
 * A [PedestrianGroup] featuring bidimensional pedestrians.
 */
interface PedestrianGroup2D<T> : PedestrianGroup<T, Euclidean2DPosition, Euclidean2DTransformation>
