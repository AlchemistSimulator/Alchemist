package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.nextDouble
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.contains
import it.unibo.alchemist.model.implementations.graph.containsAnyDestination
import it.unibo.alchemist.model.implementations.graph.destinationsWithin
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import org.apache.commons.math3.random.RandomGenerator
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

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
 * @param F the type of edges of the navigation graph provided by the environment.
 */
open class OrientingPedestrian2D<T, M : ConvexPolygon, F : GraphEdge<M>>(
    knowledgeDegree: Double,
    randomGenerator: RandomGenerator,
    environment: Euclidean2DEnvironmentWithGraph<*, T, M, F>,
    group: PedestrianGroup<T>? = null,
    /*
     * The starting width and height of the generated Ellipses will be a random
     * quantity in [minSide, maxSide] * the diameter of this pedestrian.
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0
) : AbstractOrientingPedestrian<
        T,
        Euclidean2DPosition,
        Euclidean2DTransformation,
        Ellipse,
        M,
        F
    >(knowledgeDegree, randomGenerator, environment, group), Pedestrian2D<T> {

    /*
     * Generates a random ellipse entirely contained in the given convex polygon.
     * If such polygon contains one or more destinations, the generated ellipse
     * will contain at least one of them.
     */
    override fun generateLandmarkWithin(region: M): Ellipse =
        with(region) {
            val width = randomEllipseSide()
            val height = randomEllipseSide()
            val isFinal = environment.graph().containsAnyDestination(this)
            /*
             * If is final, the center of the ellipse will be the destination (too
             * simplistic, can be modified in the future).
             */
            val origin = centroid.takeUnless { isFinal }
                ?: environment.graph()
                    .destinationsWithin(this).first() -
                    Euclidean2DPosition(width / 2, height / 2)
            val frame = Rectangle2D.Double(origin.x, origin.y, width, height)
            while (!contains(frame)) {
                /*
                 * If is final we will decrease the frame on each side by a quantity q,
                 * otherwise we just half its width and height.
                 */
                if (isFinal) {
                    val q = Euclidean2DPosition(frame.width * frameScale, frame.height * frameScale)
                    frame.setFrame(
                        frame.x + q.x,
                        frame.y + q.y,
                        frame.width - 2 * q.x,
                        frame.height - 2 * q.y)
                } else {
                    frame.width /= 2
                    frame.height /= 2
                }
            }
            Ellipse(Ellipse2D.Double(frame.x, frame.y, frame.width, frame.height))
        }

    private fun randomEllipseSide(): Double = randomGenerator.nextDouble(minSide, maxSide) * shape.diameter

    companion object {
        private const val frameScale = 0.2
    }
}
