package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.distribution.ParetoDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Selects a target based on a random direction extracted from [rng],
 * and a random distance extracted from a [ParetoDistribution] of parameters [scale] and [shape].
 * Moves toward the targets at a constant [speed] and changes targets on collision.
 */
class LevyWalk<T> @JvmOverloads constructor(
    node: Node<T>,
    reaction: Reaction<T>,
    private val env: Environment<T, Euclidean2DPosition>,
    private val rng: RandomGenerator,
    private val speed: Double,
    private val scale: Double = 1.0, // default parameters for the Pareto distribution
    private val shape: Double = 1.0
) : RandomWalker<T>(node, reaction, env, rng, speed, ParetoDistribution(rng, scale, shape)) {
    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        LevyWalk(n, r, env, rng, speed, scale, shape)
}