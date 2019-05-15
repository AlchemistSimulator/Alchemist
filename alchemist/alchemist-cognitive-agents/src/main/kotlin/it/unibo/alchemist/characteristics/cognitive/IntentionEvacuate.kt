package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.characteristics.utils.Functions

class IntentionEvacuate(
    private val desireWalkRandomly: () -> Double,
    private val desireEvacuate: () -> Double
) : BodyCognitiveCharacteristic() {

    override fun combinationFunction() =
            desireEvacuate() * Functions.logistic(logisticSigma, logisticTau,
                    wAmplifyingIntention * desireEvacuate(),
                    wInhibitingIntention * desireWalkRandomly())
}