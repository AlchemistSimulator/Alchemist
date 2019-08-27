package it.unibo.alchemist.model.interfaces.environments

/**
 * Declare boundaries.
 */
interface HasBoundaries {
    /**
     * The boundaries.
     */
    val boundaries: Boundaries
}

/**
 * Describes the boundaries via the Visitor pattern.
 * Note: whenever a new [Boundaries] is created, it should be added to a new visit method in [BoundariesVisitor].
 */
interface Boundaries {
    /**
     * [v] is the visitor in the Visitor pattern.
     * [BoundariesVisitor.visit] will be called with the right boundary type.
     */
    fun accept(v: BoundariesVisitor)
}

/**
 * The visitor to handle different types of boundaries.
 * Note: whenever a new [Boundaries] is created, it should be added to a new visit method in this interface.
 */
interface BoundariesVisitor {
    /**
     * [rectangularBoundaries] with origin in (0,0)
     */
    fun visit(rectangularBoundaries: RectangularBoundaries)
}

/**
 * Describes rectangular [Boundaries] with the origin in (0,0).
 */
class RectangularBoundaries(
    /**
     * Width
     */
    val width: Double,
    /**
     * Height
     */
    val height: Double
) : Boundaries {
    override fun accept(v: BoundariesVisitor) =
        v.visit(this)
}
