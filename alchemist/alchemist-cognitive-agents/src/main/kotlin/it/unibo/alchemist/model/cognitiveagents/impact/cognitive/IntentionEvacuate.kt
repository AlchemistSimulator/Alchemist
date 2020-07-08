package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

import it.unibo.alchemist.model.cognitiveagents.impact.cognitive.utils.logistic

/**
 * The intention to evacuate of .
 *
 * @param desireWalkRandomly
 *          the desire not to evacuate of the agent owning this characteristic.
 * @param desireEvacuate
 *          the desire to evacuate of the agent owning this characteristic.
 */
class IntentionEvacuate(
    private val desireWalkRandomly: () -> Double,
    private val desireEvacuate: () -> Double
) : BodyCognitiveCharacteristic() {

    override fun combinationFunction() =
        desireEvacuate() * logistic(
            logisticSigma, logisticTau,
            amplifyingIntentionOmega * desireEvacuate(),
            inhibitingIntentionOmega * desireWalkRandomly()
        )
}
