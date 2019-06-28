package it.unibo.alchemist.model.influencesphere.sensory

import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.influencesphere.shapes.GeometricShape2D

/**
 * A sphere of influence in the 2D world.
 */
abstract class InfluenceSphere2D<P : Position2D<P>>(private val shape: GeometricShape2D<P>) : InfluenceSphere<P> {

    override fun isInfluenced(point: P): Boolean = shape.contains(point)
}