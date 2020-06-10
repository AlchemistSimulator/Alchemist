package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D

/**
 * Defines a segment from [first] to [second] in an euclidean bidimensional space.
 */
data class Segment2DImpl<P : Vector2D<P>>(override val first: P, override val second: P) : Segment2D<P> {
    override fun copyWith(first: P, second: P): Segment2D<P> = copy(first, second)
}
