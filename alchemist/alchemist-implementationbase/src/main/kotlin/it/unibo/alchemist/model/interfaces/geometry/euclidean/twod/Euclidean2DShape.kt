package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape

/**
 * Defines an euclidean shape in a bidimensional space.
 */
typealias Euclidean2DShape = GeometricShape<Euclidean2DPosition, Euclidean2DTransformation>