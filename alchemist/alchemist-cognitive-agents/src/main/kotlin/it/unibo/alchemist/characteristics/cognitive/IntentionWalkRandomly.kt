package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.characteristics.utils.Functions

class IntentionWalkRandomly : BodyCognitiveCharacteristic() {

    override fun combinationFunction() =
            owner.desireEvacuateLevel() * Functions.logistic(logisticSigma, logisticTau,
                    wInhibitingIntention * owner.desireEvacuateLevel(),
                    wAmplifyingIntention * owner.desireWalkRandomlyLevel())
}