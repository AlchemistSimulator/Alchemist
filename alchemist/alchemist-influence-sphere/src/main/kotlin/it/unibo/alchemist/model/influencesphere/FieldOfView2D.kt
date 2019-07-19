package it.unibo.alchemist.model.influencesphere

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * A sphere of influence representing the sight of a node in the Euclidean world.
 *
 * @param env
 *          the environment where this sphere of influence is.
 * @param owner
 *          the node who owns this sphere of influence.
 * @param distance
 *          the distance in meters at which the sight arrives.
 * @param aperture
 *          the amplitude of the field of view in radians.
 */
class FieldOfView2D<T>(
    env: EuclideanPhysics2DEnvironment<T>,
    owner: Node<T>,
    distance: Double = 10.0,
    aperture: Double = Math.PI * 2 / 3 // 120° in radians
) : InfluenceSphere2D<T>(env, owner, env.shapeFactory.circleSector(distance, aperture, -Math.PI / 2))