package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

/**
 * The belief of the situation dangerousness.
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

    private fun List<CognitiveAgent>.aggregateDangerBeliefs() =
        if (size > 0) {
            this.sumByDouble { it.dangerBelief() } / size
        } else 0.0
}