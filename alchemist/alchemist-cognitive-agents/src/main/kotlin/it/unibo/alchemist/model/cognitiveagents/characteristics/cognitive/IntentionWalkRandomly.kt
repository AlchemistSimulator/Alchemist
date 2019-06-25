package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import it.unibo.alchemist.model.cognitiveagents.characteristics.utils.logistic

class IntentionWalkRandomly(
    private val desireWalkRandomly: () -> Double,
    private val desireEvacuate: () -> Double
) : BodyCognitiveCharacteristic() {

    override fun combinationFunction() =
        desireEvacuate() * logistic(
            logisticSigma, logisticTau,
            inhibitingIntentionOmega * desireEvacuate(),
            amplifyingIntentionOmega * desireWalkRandomly()
        )
}