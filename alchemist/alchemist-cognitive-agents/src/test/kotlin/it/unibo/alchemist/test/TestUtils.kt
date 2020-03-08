package it.unibo.alchemist.test

import com.uchuhimo.konf.Config
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

/**
 * Provides an empty [NavigationGraph].
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> emptyNavigationGraph(): NavigationGraph<V, A, N, E> =
    NavigationGraphBuilder<V, A, N, E>().build(mutableListOf())

/**
 * Provides an empty [NavigationGraph] in an euclidean bidimensional space.
 */
fun emptyNavigationGraph2D(): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, *, *> =
    emptyNavigationGraph()

typealias NavigationGraph2D = NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>

const val PATH_TO_NAVIGATION_GRAPH = "navigation-graphs/"

/**
 * Generates a navigation graph of an environment from a configuration file. The name of
 * the file, the zoom of the environment and the destinations are to be specified.
 */
fun envGraphFromConf(confFile: String, zoom: Double, destinations: Collection<Euclidean2DPosition>): NavigationGraph2D {
    val config = Config {
        addSpec(EnvironmentSpec)
        addSpec(SeedsSpec)
        addSpec(CrossingsSpec)
    }.from.toml.resource("navigation-graphs/$confFile")
    val name = config[EnvironmentSpec.name]
    val scaler = zoom / config[EnvironmentSpec.zoom]
    val envStartX = config[EnvironmentSpec.startX]
    val envStartY = config[EnvironmentSpec.startY]
    val width = config[EnvironmentSpec.width]
    val height = config[EnvironmentSpec.height]
    val seedsPositions = config[SeedsSpec.positions]
    val side = config[SeedsSpec.side]
    val crossingsSide = config[CrossingsSpec.side]
    return Deaccon2D().generateEnvGraph(
        Point2D.Double(envStartX * scaler, envStartY * scaler),
        width * scaler,
        height * scaler,
        ImageEnvironment<Number>("images/$name", zoom).obstacles,
        seedsPositions.map {
            Point2D.Double(it[0], it[1]).times(scaler)
        },
        side * scaler,
        destinations,
        crossingsSide * scaler
    )
}

fun Point2D.times(n: Double) = Point2D.Double(x * n, y * n)
