package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

/**
 * The desire not to evacuate.
 *
 * @param compliance
 *          the current level of compliance of the agent owning this.
 * @param dangerBelief
 *          the current level of danger belief of the agent owning this.
 * @param fear
 *          the current level of fear of the agent owning this.
 */
class DesireWalkRandomly(
    private val compliance: Double,
    private val dangerBelief: () -> Double,
    private val fear: () -> Double
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
        compliance * (1 - maxOf(inhibitingWalkRandOmega * dangerBelief(), inhibitingWalkRandOmega * fear()))
}
