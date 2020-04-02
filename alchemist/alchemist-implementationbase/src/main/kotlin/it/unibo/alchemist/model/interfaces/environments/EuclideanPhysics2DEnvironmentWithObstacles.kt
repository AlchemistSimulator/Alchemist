package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles
import it.unibo.alchemist.model.interfaces.Obstacle2D

/**
 * Euclidean physics environment with support for obstacles.
 */
interface EuclideanPhysics2DEnvironmentWithObstacles<W : Obstacle2D, T> :
    Physics2DEnvironment<T>,
    Environment2DWithObstacles<W, T, Euclidean2DPosition>
