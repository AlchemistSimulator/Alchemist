package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation

/**
 * Defines a bidimensional euclidean environment.
 *
 * @param <T> nodes' concentration type
 */
interface EuclideanPhysics2DEnvironment<T>
    : PhysicsEnvironment<T, Euclidean2DPosition, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>