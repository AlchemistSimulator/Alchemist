package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.characteristics.utils.logistic

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