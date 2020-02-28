package it.unibo.alchemist.model.implementations.graph

import it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon.Deaccon2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import java.awt.Shape
import java.awt.geom.Point2D

/**
 */
fun orientingSimulationEnvGraph(envObstacles: Collection<Shape>): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing> =
     Deaccon2D().generateEnvGraph(Point2D.Double(0.0, 0.0), 150.0, 150.0, envObstacles, mutableListOf(
        Point2D.Double(15.0, 15.0),
        Point2D.Double(15.0, 42.0),
        Point2D.Double(62.0, 15.0),
        Point2D.Double(60.0, 42.0),
        Point2D.Double(83.0, 42.0),
        Point2D.Double(85.0, 15.0),
        Point2D.Double(132.0, 15.0),
        Point2D.Double(132.0, 42.0),
        Point2D.Double(14.0, 86.0),
        Point2D.Double(72.0, 108.0),
        Point2D.Double(130.0, 117.0),
        Point2D.Double(70.0, 132.0),
        Point2D.Double(14.0, 134.0),
        Point2D.Double(38.0, 74.0),
        Point2D.Double(111.0, 74.0),
        Point2D.Double(37.0, 13.0),
        Point2D.Double(37.0, 136.0),
        Point2D.Double(109.0, 136.0),
        Point2D.Double(109.0, 11.0)
     ), 1.0, mutableListOf(), 2.5)

/**
 */
fun congestionAvoidanceEnvGraph(envObstacles: Collection<Shape>): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing> =
    Deaccon2D().generateEnvGraph(Point2D.Double(0.0, 0.0), 90.0, 70.0, envObstacles, mutableListOf(
        Point2D.Double(80.0, 50.0),
        Point2D.Double(53.0, 45.0),
        Point2D.Double(12.0, 61.0),
        Point2D.Double(25.0, 38.0),
        Point2D.Double(40.0, 55.0),
        Point2D.Double(40.0, 43.0),
        Point2D.Double(35.0, 32.0)
    ), 2.0, mutableListOf(), 2.5)
