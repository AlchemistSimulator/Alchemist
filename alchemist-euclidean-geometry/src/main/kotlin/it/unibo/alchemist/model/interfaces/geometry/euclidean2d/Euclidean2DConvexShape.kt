package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexShape

/**
 * A convex euclidean shape in a bidimensional environment.
 */
typealias Euclidean2DConvexShape = ConvexShape<Euclidean2DPosition, Euclidean2DTransformation>
