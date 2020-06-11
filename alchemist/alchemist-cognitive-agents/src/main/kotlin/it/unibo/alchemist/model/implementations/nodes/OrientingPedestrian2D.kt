package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

private typealias AbstractOrientingPedestrianAlias<T, M, E> = AbstractOrientingPedestrian<
    T, Euclidean2DPosition, Euclidean2DTransformation, Ellipse, M, E, Euclidean2DShapeFactory>
/**
 * An [OrientingPedestrian] in an euclidean bidimensional space.
 * This class defines the method responsible for the creation of landmarks, which
 * are represented as [Ellipse]s. These can represent both the human error concerning
 * the exact position of a landmark inside the ellipse and the error concerning the
 * angles formed by the connections between landmarks.
 * This class accepts an environment whose graph contains nodes which are (subclasses
 * of) [ConvexPolygon]s.
 *
 * @param T the concentration type.
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 */
open class OrientingPedestrian2D<T, M : ConvexPolygon, E> @JvmOverloads constructor(
    override val environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, E>,
    randomGenerator: RandomGenerator,
    knowledgeDegree: Double,
    /*
     * The starting width and height of the generated Ellipses will be a random
     * quantity in [minSide, maxSide] * the diameter of this pedestrian.
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0,
    group: PedestrianGroup<T, Euclidean2DPosition, Euclidean2DTransformation>? = null
) : AbstractOrientingPedestrianAlias<T, M, E> (
    knowledgeDegree,
    randomGenerator,
    environment,
    group
), Pedestrian2D<T> {

    /*
     * Generates a random ellipse entirely contained in the given convex polygon.
     * If such polygon contains one or more destinations, the generated ellipse
     * will contain at least one of them.
     */
    override fun generateLandmarkWithin(region: M): Ellipse =
        with(region) {
            val width = randomEllipseSide()
            val height = randomEllipseSide()
            val frame = Rectangle2D.Double(centroid.x, centroid.y, width, height)
            while (!contains(frame)) {
                frame.width /= 2
                frame.height /= 2
            }
            Ellipse(
                Ellipse2D.Double(
                    frame.x,
                    frame.y,
                    frame.width,
                    frame.height
                )
            )
        }

    private fun randomEllipseSide(): Double = randomGenerator.nextDouble(minSide, maxSide) * shape.diameter

    override val shape by lazy { super<Pedestrian2D>.shape }
    final override val fieldOfView by lazy { super.fieldOfView }

    init {
        senses += fieldOfView
    }
}
