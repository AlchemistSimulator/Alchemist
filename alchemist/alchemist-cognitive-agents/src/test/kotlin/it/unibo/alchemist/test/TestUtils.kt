package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.implementations.environments.ImageEnvironment
import it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon.Deaccon2D
import it.unibo.alchemist.model.implementations.graph.Euclidean2DCrossing
import it.unibo.alchemist.model.implementations.graph.builder.NavigationGraphBuilder
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import org.kaikikm.threadresloader.ResourceLoader
import java.awt.Shape
import java.awt.geom.Point2D

/**
 * Run the simulation this environment owns.
 *
 * @param initialized
 *          the lambda to execute when the simulation begins.
 * @param stepDone
 *          the lambda to execute on each step of the simulation.
 * @param finished
 *          the lambda to execute at the end of the simulation.
 * @param numSteps
 *          the number of steps the simulation must execute.
 */
fun <T, P : Position<out P>> Environment<T, P>.startSimulation(
    initialized: (e: Environment<T, P>) -> Unit = { },
    stepDone: (e: Environment<T, P>, r: Reaction<T>, t: Time, s: Long) -> Unit = { _, _, _, _ -> Unit },
    finished: (e: Environment<T, P>, t: Time, s: Long) -> Unit = { _, _, _ -> Unit },
    numSteps: Long = 10000
): Environment<T, P> = Engine(this, numSteps).apply {
    addOutputMonitor(object : OutputMonitor<T, P> {
        override fun initialized(e: Environment<T, P>) = initialized.invoke(e)
        override fun stepDone(e: Environment<T, P>, r: Reaction<T>, t: Time, s: Long) = stepDone.invoke(e, r, t, s)
        override fun finished(e: Environment<T, P>, t: Time, s: Long) = finished.invoke(e, t, s)
    })
    play()
    run()
    error.ifPresent { throw it }
}.environment

/**
 * Loads a simulation from a YAML file.
 *
 * @param resource
 *          the name of the file containing the simulation to load.
 * @param vars
 *          a map specifying name-value bindings for the variables in this scenario.
 */
fun <T, P : Position<P>> loadYamlSimulation(resource: String, vars: Map<String, Double> = emptyMap()): Environment<T, P> =
    with(ResourceLoader.getResourceAsStream(resource)) {
        YamlLoader(this).getWith<T, P>(vars)
    }

fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> emptyNavigationGraph(): NavigationGraph<V, A, N, E> =
    NavigationGraphBuilder<V, A, N, E>().build(mutableListOf())

fun emptyNavigationGraph2D(): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, *, *> =
    emptyNavigationGraph()

typealias NavigationGraph2D = NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>

/**
 */
fun buildingPlanimetryEnvGraph(zoom: Double, destinations: Collection<Euclidean2DPosition>): NavigationGraph2D =
    Deaccon2D().generateEnvGraph(
        Point2D.Double(0.0, 0.0),
        150.0,
        150.0,
        ImageEnvironment<Number>("images/building-planimetry.png", zoom).obstacles,
        mutableListOf(
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
        ),
        1.0,
        destinations,
        2.5
    )
/**
 */
fun congestionAvoidanceEnvGraph(zoom: Double, destinations: Collection<Euclidean2DPosition>): NavigationGraph2D =
    Deaccon2D().generateEnvGraph(
        Point2D.Double(0.0, 0.0),
        90.0,
        70.0,
        ImageEnvironment<Number>("images/congestion-avoidance.png", zoom).obstacles,
        mutableListOf(
            Point2D.Double(80.0, 50.0),
            Point2D.Double(53.0, 45.0),
            Point2D.Double(12.0, 61.0),
            Point2D.Double(25.0, 38.0),
            Point2D.Double(40.0, 55.0),
            Point2D.Double(40.0, 43.0),
            Point2D.Double(35.0, 32.0)
        ),
        2.0,
        destinations,
        2.5
    )
