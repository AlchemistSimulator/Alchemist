package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape

/**
 * A convex euclidean shape in a bidimensional environment.
 */
typealias Euclidean2DConvexShape = ConvexGeometricShape<Euclidean2DPosition, Euclidean2DTransformation>
