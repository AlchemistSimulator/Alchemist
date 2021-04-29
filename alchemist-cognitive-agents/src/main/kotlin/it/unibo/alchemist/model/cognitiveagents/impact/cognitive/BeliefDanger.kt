package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

import it.unibo.alchemist.meanOrZero
import it.unibo.alchemist.model.cognitiveagents.CognitiveAgent

/**
 * The perception of the situation dangerousness.
 * The name belief derives from the [IMPACT model](https://doi.org/10.1007/978-3-319-70647-4_11).
 *
 * @param zoneDangerousness
 *          the influence of the position of the agent owning this
 *          compared to the real position of the source of danger.
 * @param fear
 *          the level of fear of the agent owning this.
 * @param influencialPeople
 *          the list of cognitive agents with an influence on the agent owning this.
 */
class BeliefDanger(
    private val zoneDangerousness: () -> Double,
    private val fear: () -> Double,
    private val influencialPeople: () -> List<CognitiveAgent>
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction(): Double = maxOf(
        sensingOmega * zoneDangerousness(),
        persistingOmega * level(),
        (affectiveBiasingOmega * fear() + influencialPeople().aggregateDangerBeliefs()) / (affectiveBiasingOmega + 1)
    )

    private fun List<CognitiveAgent>.aggregateDangerBeliefs() = meanOrZero { cognitive.dangerBelief() }
}
