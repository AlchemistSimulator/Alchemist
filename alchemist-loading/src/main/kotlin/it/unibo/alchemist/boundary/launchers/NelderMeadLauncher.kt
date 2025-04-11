package it.unibo.alchemist.boundary.launchers

import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.model.Environment
import it.unibo.common.NelderMeadMethod
import it.unibo.common.Vertex
import org.danilopianini.rrmxmx.RrmxmxRandom
import org.danilopianini.rrmxmx.RrmxmxRandom.Companion.DEFAULT_SEED
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.Int.Companion.MAX_VALUE

class NelderMeadLauncher
@JvmOverloads
constructor(
    private val objectiveFunction: Environment<*, *>.() -> Double,
    private val variables: List<String> = emptyList(),
    private val seedName: String,
    private val repetitions: Int = 1,
    private val maxIterations: Int = MAX_VALUE,
    private val seed: ULong = DEFAULT_SEED,
    private val tolerance: Double = 1e-6,
    private val alpha: Double = 1.0, // standard value for the reflection in Nelder-Mead method
    private val gamma: Double = 2.0, // standard value for the expansion in Nelder-Mead method
    private val rho: Double = 0.5, // standard value for the contraction in Nelder-Mead method
    private val sigma: Double = 0.5, // standard value for the shrink in Nelder-Mead method
) : Launcher {
    @Synchronized
    override fun launch(loader: Loader) {
        require(loader.variables.isNotEmpty() || variables.isNotEmpty()) {
            "No variables found, can not optimize anything."
        }
        val simplexVertices: List<Map<String, Double>> = generateSymplexVertices(loader.variables)
        val seeds: List<Int> =
            loader.variables[seedName]
                ?.stream()
                ?.map {
                    check(it is Number) { "Seed must be a number. $it is not." }
                    it.toInt()
                }?.toList()
                ?.take(repetitions) ?: listOf(repetitions)
        val executorID = AtomicInteger(0)
        val executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) {
                Thread(it, "Alchemist Nelder Mead worker #${executorID.getAndIncrement()}")
            }
        val errorQueue = ConcurrentLinkedDeque<Throwable>()
        loader.executeWithNelderMead(simplexVertices, executor) { vertex ->
            val futureValues = seeds.map<Int, Future<Double>> { currentSeed ->
                // associate keys to vertex values
                val simulationParameters = variables
                    .associateWith { vertex[variables.indexOf(it)] } + (seedName to currentSeed)
                check(loader.variables.keys == simulationParameters.keys) {
                    "Variables do not match: ${loader.variables.keys} != ${simulationParameters.keys}"
                }
                executor.submit<Double>(
                    Callable {
                        val simulation = loader.getWith<Any?, Nothing>(simulationParameters)
                        simulation.play()
                        simulation.run()
                        if (simulation.error.isPresent) {
                            errorQueue.add(simulation.error.get())
                        }
                        objectiveFunction(simulation.environment)
                    },
                )
            }
            ForkJoinPool.commonPool().submit(
                Callable {
                    futureValues.map { it.get() }.average()
                },
            )
        }.also {
            // write the result into a csv with as name the variables and the date of execution
            val outputPath =
                Paths.get("").toAbsolutePath().toString() + "${File.separator}data${File.separator}NelderMeadMethod"
            // if not exists create the directory
            File(outputPath).mkdirs()
            val outputFile =
                File(
                    "$outputPath${File.separator}${variables.joinToString(
                        "_",
                    )}_maxIter${maxIterations}_${seedName}s${seeds.max()}_${LocalDateTime.now().toString().replace(
                        ":",
                        "-",
                    )}.csv",
                )
            val outputContent = buildString {
                append(variables.joinToString(" "))
                append("\n")
                append(it.entries.joinToString(" ") { it.value.toString() })
                append("\n")
            }
            outputFile.writeText(outputContent)
        }
        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS)
        if (errorQueue.isNotEmpty()) {
            throw errorQueue.reduce { previous, other ->
                previous.addSuppressed(other)
                previous
            }
        }
    }

    private fun Loader.executeWithNelderMead(
        simplexVertices: List<Map<String, Double>>,
        executorService: ExecutorService,
        executeFunction: (List<Double>) -> Future<Double>,
    ): Map<String, Double> = NelderMeadMethod(
        simplex = simplexVertices.map { Vertex(it) },
        maxIterations = maxIterations,
        tolerance = tolerance,
        alpha = alpha,
        gamma = gamma,
        rho = rho,
        sigma = sigma,
        executorService = executorService,
        objective = executeFunction,
    ).optimize()
        .let { result ->
            this@NelderMeadLauncher.variables.associateWith {
                result[this@NelderMeadLauncher.variables.indexOf(it)]
            }
        }

    private fun generateSymplexVertices(loaderVariables: Map<String, Variable<*>>): List<Map<String, Double>> {
        val randomGenerator = RrmxmxRandom(seed)
        val instances: Map<String, ClosedRange<Double>> =
            variables.associateWith { varName ->
                val variable = loaderVariables.getValue(varName)
                val allValues =
                    variable
                        .stream()
                        .map {
                            check(it is Number) {
                                "All variables to optimize must be Numbers. $varName has value $it."
                            }
                            it.toDouble()
                        }.toList()
                allValues.min()..allValues.max()
            }
        return (0..variables.size).map {
            instances.mapValues { (_, range) -> randomGenerator.nextDouble(range.start, range.endInclusive) }
        }
    }
}
