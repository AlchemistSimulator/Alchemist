package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.characteristics.utils.Functions

class IntentionEvacuate : BodyCognitiveCharacteristic() {

    override fun combinationFunction() =
            owner.desireEvacuateLevel() * Functions.logistic(logisticSigma, logisticTau,
                    wAmplifyingIntention * owner.desireEvacuateLevel(),
                    wInhibitingIntention * owner.desireWalkRandomlyLevel())
}