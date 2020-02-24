package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.nextDouble
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.Ellipse
import it.unibo.alchemist.model.implementations.geometry.graph.containsDestination
import it.unibo.alchemist.model.implementations.geometry.graph.destinationsWithin
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

/**
 * An [AbstractOrientingPedestrian] in an Euclidean bidimensional space.
 * It defines the method responsible for the creation of landmarks.
 *
 * @param E the type of edges of the environment's graph.
 */
abstract class AbstractOrientingPedestrian2D<T, E : GraphEdge<ConvexPolygon>>(
    knowledgeDegree: Double,
    private val rg: RandomGenerator,
    private val envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, E>,
    env: Environment<T, Euclidean2DPosition>,
    group: PedestrianGroup<T>? = null
) : AbstractOrientingPedestrian<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, E, Ellipse, T>(knowledgeDegree, rg, envGraph, env, group),
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
     * Generate a random ellipse entirely contained in the given convex polygon.
     * If such polygon contains one or more destinations, the generated ellipse
     * will contain at least one of them.
     */
    override fun generateLandmarkWithin(region: ConvexPolygon): Ellipse =
        with(region) {
            val w = rg.nextDouble(MIN_SIDE, MAX_SIDE) * shape.diameter
            val h = rg.nextDouble(MIN_SIDE, MAX_SIDE) * shape.diameter
            val isFinal = envGraph.containsDestination(this)
            /*
             * If is final, the center of the ellipse will be the destination (too simplistic,
             * can be modified in the future).
             */
            val o = if (isFinal) {
                envGraph.destinationsWithin(this).first() - Euclidean2DPosition(w / 2, h / 2)
            } else {
                centroid
            }
            /*
             * The frame in which the ellipse is inscribed.
             */
            val f = Rectangle2D.Double(o.x, o.y, w, h)
            while (!contains(f)) {
                /*
                 * If is final we will decrease the frame on each side by a quantity q,
                 * otherwise we just half its width and height.
                 */
                if (isFinal) {
                    val q = Euclidean2DPosition(f.width * 0.2, f.height * 0.2)
                    f.setFrame(f.x + q.x, f.y + q.y, f.width - 2 * q.x, f.height - 2 * q.y)
                } else {
                    f.width /= 2
                    f.height /= 2
                }
            }
            Ellipse(Ellipse2D.Double(f.x, f.y, f.width, f.height))
        }

    private fun ConvexPolygon.contains(s: Shape): Boolean = s.vertices().all { contains(it) }
}
