package it.unibo.alchemist.boundary

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Nelder-Mead optimization method.
 * Given an initial [simplex], this method iteratively refines the simplex to minimize a given [objective] function.
 * The method is suitable for optimizing functions that are continuous but not differentiable.
 * Other parameters are:
 * - [alpha]: Reflection coefficient (standard value is 1.0);
 * - [gamma]: Expansion coefficient (standard value is 2.0);
 * - [rho]: Contraction coefficient (standard value is 0.5);
 * - [sigma]: Shrink coefficient (standard value is 0.5);
 * - [maxIterations]: Maximum number of iterations;
 * - [tolerance]: Termination condition (small variation in function values).
 */
class NelderMeadMethod(
    val simplex: List<Vertex>,
    private val maxIterations: Int,
    private val tolerance: Double,
    private val alpha: Double, // Reflection coefficient
    private val gamma: Double, // Expansion coefficient
    private val rho: Double, // Contraction coefficient
    private val sigma: Double, // Shrink coefficient
    executorService: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
    private val objective: (List<Double>) -> Future<Double>,
) {

    private val cache = Caffeine.newBuilder().executor(executorService)
        .build<List<Double>, Future<Double>> { coordinates ->
            objective(coordinates)
        }

    /**
     * Apply the Nelder-Mead optimization method to the given [simplex] and [objective] function.
     */
    fun optimize(): Vertex {
        require(simplex.isNotEmpty()) { "The initial simplex must not be empty" }
        val dimensions = simplex.first().size
        require(dimensions > 0) { "The number of dimensions must be greater than 0" }
        require(simplex.size == dimensions + 1) {
            "The vertices of the initial simplex must be one more than the number of dimensions"
        }
        require(simplex.all { it.size == dimensions }) {
            "All vertices of the initial simplex must have the same number of dimensions"
        }
        var symplexUpdated = simplex
        repeat(maxIterations) {
            // Sort simplex by function values
            val sortedSimplex = symplexUpdated
                .map { it to cache[it.valuesToList()] }
                .sortedBy { it.second.get() }
                .map { it.first }
            val bestVertex: Vertex = sortedSimplex.first()
            val worstVertex: Vertex = sortedSimplex.last()
            val secondWorstVertex: Vertex = sortedSimplex[simplex.size - 2]
            val bestValue = cache[bestVertex.valuesToList()]
            val worstValues = worstVertex.valuesToList()
            // Compute centroid (excluding worst point)
            val centroid =
                DoubleArray(dimensions) { index ->
                    sortedSimplex.dropLast(1).sumOf { it[index] } / (sortedSimplex.size - 1)
                }.toList()
            // Reflections
            val reflected: List<Double> = centroid.mapCentroid(alpha, worstValues)
            val reflectedValue = cache[reflected]
            require(reflectedValue.get().isFinite() && !reflectedValue.get().isNaN()) {
                "Invalid objective function return value for reflection with $reflected = $reflectedValue.\n" +
                    "Check the objective function implementation, the result should be a finite number."
            }
            check (!reflectedValue.get().isFinite()) {
                error("Invalid objective function return value for reflection")
            }
            val newSimplex = when {
                reflectedValue < bestValue -> { // expansion
                    val expanded: List<Double> = centroid.mapCentroid(gamma, reflected)
                    when {
                        cache[expanded] < reflectedValue -> sortedSimplex.updateLastVertex(expanded)
                        else -> sortedSimplex.updateLastVertex(reflected)
                    }
                }
                reflectedValue < cache[secondWorstVertex.valuesToList()] -> { // accept reflection
                    sortedSimplex.updateLastVertex(reflected)
                }
                else -> { // contraction
                    val contracted = when {
                        reflectedValue < cache[worstValues] -> centroid.mapCentroid(rho, reflected)
                        else -> centroid.mapCentroid(rho, worstValues)
                    }
                    when {
                        cache[contracted] < cache[worstValues] -> sortedSimplex.updateLastVertex(contracted)
                        else -> { // shrink simplex
                            sortedSimplex.map { vertex ->
                                Vertex(
                                    vertex.keys().associateWith { key ->
                                        // Find the index corresponding to the key
                                        val index = bestVertex.keys().indexOf(key)
                                        val oldValue = bestVertex[index]
                                        oldValue + sigma * (vertex[index] - oldValue) // Apply shrink transformation
                                    },
                                )
                            }
                        }
                    }
                }
            }
            // Check termination condition (small variation in function values)
            val functionValues = newSimplex.map { cache[it.valuesToList()].get() }
            symplexUpdated = newSimplex
            val maxDiff = functionValues.maxOrNull()!! - functionValues.minOrNull()!!
            if (maxDiff < tolerance) return symplexUpdated.first()
        }
        return symplexUpdated.first()
    }

    private fun List<Double>.mapCentroid(coefficient: Double, values: List<Double>): List<Double> =
        mapIndexed { index, value -> value + coefficient * (values[index] - value) }

    private fun List<Vertex>.updateLastVertex(newVertex: List<Double>): List<Vertex> = mapIndexed { index, vertex ->
        if (index == size - 1) {
            Vertex(vertex.keys().associateWith { newVertex[vertex.keys().indexOf(it)] })
        } else {
            vertex
        }
    }

    /**
     * This companion object contains the comparison operators for [Future] of [Double] objects and [Double]s.
     */
    companion object {
        /**
         * Compares two [Future] of [Double] objects.
         */
        operator fun Future<Double>.compareTo(other: Future<Double>): Int = get().compareTo(other.get())

        /**
         * Compares a [Future] of [Double] object with a [Double].
         */
        operator fun Future<Double>.compareTo(other: Double): Int = get().compareTo(other)

        /**
         * Compares a [Double] with a [Future] of [Double] object.
         */
        operator fun Double.compareTo(other: Future<Double>): Int = compareTo(other.get())
    }
}

/**
 * A vertex of the simplex in the Nelder-Mead method.
 */
@JvmInline
value class Vertex(private val vertex: Map<String, Double>) {
    /**
     * Returns amount of dimensions of the vertex.
     */
    val size: Int
        get() = vertex.size

    /**
     * Returns the values of the vertex as a list.
     */
    fun valuesToList(): List<Double> = vertex.values.toList()

    /**
     * Returns the keys of the vertex as a set.
     */
    fun keys(): Set<String> = vertex.keys

    /**
     * Returns the value of the vertex at the given index.
     */
    operator fun get(index: Int) = valuesToList()[index]

    /**
     * Returns the key of the vertex at the given index.
     */
    fun keyAt(index: Int) = vertex.keys.elementAt(index)
}
