package it.unibo.alchemist.model.smartcam

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.optim.linear.LinearConstraint
import org.apache.commons.math3.optim.linear.LinearConstraintSet
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction
import org.apache.commons.math3.optim.linear.NonNegativeConstraint
import org.apache.commons.math3.optim.linear.Relationship
import org.apache.commons.math3.optim.linear.SimplexSolver
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/**
 * Given a list of sources (cameras) and a list of destinations (targets), decides which camera gets which target.
 * @param <S> source type
 * @param <D> destination type
 */
class CameraTargetAssignmentProblem<S, D> {
    /**
     * Given a list of sources and a list of destinations, decides which source gets which destination.
     * @param sources all possible sources
     * @param destinations all possible destinations
     * @param maxSourcesPerDestination maximum number of sources for each destination
     * @param cost a function calculating the cost for a source to reach the given destination.
     * @return a map from sources to destinations
     */
    fun solve(sources: List<S>, destinations: List<D>, maxSourcesPerDestination: Int, cost: (source: S, destination: D) -> Double): Map<S, D> {
        if (sources.isEmpty() || destinations.isEmpty() || maxSourcesPerDestination <= 0) {
            return emptyMap()
        }
        val hasFakeDestination = sources.size > maxSourcesPerDestination * destinations.size
        // Create a fake destination for the surplus sources to adhere to the maxSourcesPerDestination rule.
        val totalDestinations = if (hasFakeDestination) destinations.size + 1 else destinations.size
        /*
         * MIN z = Summation of Cij * Xij where i = source number and j = destination number
         */
        var maxCost = 0.0
        val objectiveFunctionCoefficients = ArrayRealVector(sources.size * totalDestinations).also {
            var i = 0
            repeat(sources.size) { src ->
                repeat(destinations.size) { dest ->
                    val thisCost = cost.invoke(sources[src], destinations[dest])
                    if (thisCost > maxCost) {
                        maxCost = thisCost
                    }
                    it.setEntry(i++, thisCost)
                }
                if (hasFakeDestination) {
                    i++ // empty spot for the fake destination. It will be filled later when the maxCost is calculated
                }
            }
            if (hasFakeDestination) { // add the costs to the fake destinations for all the sources
                repeat(sources.size) { src ->
                    // the fake destination is the most expensive in order to be the last choice
                    it.setEntry(src * totalDestinations + destinations.size, maxCost * 2)
                }
            }
        }
        val objectiveFunction = LinearObjectiveFunction(objectiveFunctionCoefficients, 0.0)
        /*
         * Subject to:
         */
        val constraints = LinearConstraintSet(ArrayList<LinearConstraint>(sources.size + totalDestinations * 2).also {
            /*
             * All sources must get exactly one destination.
             * So the summation of all the destinations assigned to a source must be one.
             */
            repeat(sources.size) { srcIdx ->
                val coefficients = DoubleArray(sources.size * totalDestinations)
                repeat(totalDestinations) { dstIdx ->
                    coefficients[srcIdx * totalDestinations + dstIdx] = 1.0
                }
                it.add(LinearConstraint(coefficients, Relationship.EQ, 1.0))
            }
            /*
             * Maximum and minimum amount of sources per destination. The fake destination has no such constraints.
             */
            repeat(destinations.size) { dstIdx ->
                val coefficients = DoubleArray(sources.size * totalDestinations)
                repeat(sources.size) { srcIdx ->
                    coefficients[srcIdx * totalDestinations + dstIdx] = 1.0
                }
                val sourcesPerDestination = min(maxSourcesPerDestination.toDouble(), sources.size.toDouble() / destinations.size)
                if (sources.size.toDouble() % destinations.size == 0.0) {
                    // if we can save constraints we do so
                    it.add(LinearConstraint(coefficients, Relationship.EQ, sourcesPerDestination))
                } else {
                    it.add(LinearConstraint(coefficients, Relationship.GEQ, floor(sourcesPerDestination)))
                    it.add(LinearConstraint(coefficients, Relationship.LEQ, ceil(sourcesPerDestination)))
                }
            }
        })
        val solution = SimplexSolver().optimize(
            objectiveFunction,
            constraints,
            GoalType.MINIMIZE,
            NonNegativeConstraint(true))
        val sourceToDestination = mutableMapOf<S, D>()
        solution.first
            .forEachIndexed { idx, value ->
                if (value > 0.0 && idx % totalDestinations < destinations.size) { // exclude the fake destination which counts as a zero
                    val source = sources[idx / totalDestinations]
                    sourceToDestination[source] = destinations[idx % totalDestinations]
                }
            }
        return sourceToDestination
    }
}
