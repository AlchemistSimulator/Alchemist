package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.interfaces.geometry.ConvexShape
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A convex euclidean shape in a bidimensional environment.
 */
typealias Euclidean2DConvexShape = ConvexShape<Euclidean2DPosition, Euclidean2DTransformation>
