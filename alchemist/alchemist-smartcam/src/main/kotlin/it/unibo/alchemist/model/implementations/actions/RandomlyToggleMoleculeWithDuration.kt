package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.utils.nextDouble
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.random.RandomGenerator

/**
 * When the [molecule] is off, rolls the dices according to [odds] and if it is time to toggle, sets the [concentration]
 * and choses a random duration in the interval [[minDuration], [maxDuration]].
 * The [molecule] will be removed after the chosen duration has elapsed.
 */
class RandomlyToggleMoleculeWithDuration<T>(
    node: Node<T>,
    private val reaction: Reaction<T>,
    private val rng: RandomGenerator,
    molecule: Molecule,
    concentration: T,
    private val odds: Double,
    private val minDuration: Double,
    private val maxDuration: Double
) : RandomlyToggleMolecule<T>(node, rng, molecule, concentration, odds) {

    init {
        require(maxDuration >= minDuration)
    }

    private var duration = 0.0

    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        RandomlyToggleMoleculeWithDuration(n, r, rng, molecule, concentration, odds, minDuration, maxDuration)

    override fun execute() {
        duration -= 1 / reaction.rate
        super.execute()
    }

    override fun shouldToggle() = when {
        isOn() -> duration <= 0
        super.shouldToggle() -> {
            duration = rng.nextDouble(minDuration, maxDuration)
            true
        }
        else -> false
    }
}
