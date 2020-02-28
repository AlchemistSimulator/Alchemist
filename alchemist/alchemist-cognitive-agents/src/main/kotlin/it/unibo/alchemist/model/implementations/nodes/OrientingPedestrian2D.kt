package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.nextDouble
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse
import it.unibo.alchemist.model.implementations.graph.containsDestination
import it.unibo.alchemist.model.implementations.graph.destinationsWithin
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

/**
 * An orienting pedestrian in an euclidean bidimensional space.
 * This class defines the method responsible for the creation of landmarks: in
 * particular, it represents landmarks as [Ellipse]s and accepts an [environmentGraph]
 * whose nodes are [ConvexPolygon]s.
 *
 * @param N1 the type of nodes of the [environmentGraph].
 * @param E1 the type of edges of the [environmentGraph].
 * @param T the concentration type.
 */
open class OrientingPedestrian2D<N1 : ConvexPolygon, E1 : GraphEdge<N1>, T>(
    knowledgeDegree: Double,
    randomGenerator: RandomGenerator,
    environmentGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, N1, E1>,
    environment: Environment<T, Euclidean2DPosition>,
    group: PedestrianGroup<T>? = null
) : AbstractOrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, N1, E1, Ellipse, T>(knowledgeDegree, randomGenerator, environmentGraph, environment, group),
    Pedestrian2D<T> {

    /*
     * The starting width and height of the generated Ellipses
     * will be a random quantity in [MIN_SIDE, MAX_SIDE] * the
     * diameter of this pedestrian.
     */
    companion object {
        private const val MIN_SIDE = 30.0
        private const val MAX_SIDE = 60.0
    }

    /*
     * Generates a random ellipse entirely contained in the given convex polygon.
     * If such polygon contains one or more destinations, the generated ellipse
     * will contain at least one of them.
     */
    override fun generateLandmarkWithin(region: N1): Ellipse =
        with(region) {
            val width = randomGenerator.nextDouble(MIN_SIDE, MAX_SIDE) * shape.diameter
            val height = randomGenerator.nextDouble(MIN_SIDE, MAX_SIDE) * shape.diameter
            val isFinal = environmentGraph.containsDestination(this)
            /*
             * If is final, the center of the ellipse will be the destination (too simplistic,
             * can be modified in the future).
             */
            val origin = centroid.takeUnless { isFinal }
                ?: environmentGraph.destinationsWithin(this).first() - Euclidean2DPosition(width / 2, height / 2)
            val frame = Rectangle2D.Double(origin.x, origin.y, width, height)
            while (!contains(frame)) {
                /*
                 * If is final we will decrease the frame on each side by a quantity q,
                 * otherwise we just half its width and height.
                 */
                if (isFinal) {
                    val q = Euclidean2DPosition(frame.width * 0.2, frame.height * 0.2)
                    frame.setFrame(frame.x + q.x, frame.y + q.y, frame.width - 2 * q.x, frame.height - 2 * q.y)
                } else {
                    frame.width /= 2
                    frame.height /= 2
                }
            }
            Ellipse(Ellipse2D.Double(frame.x, frame.y, frame.width, frame.height))
        }

    private fun ConvexPolygon.contains(s: Shape): Boolean = s.vertices().all { contains(it) }
}
