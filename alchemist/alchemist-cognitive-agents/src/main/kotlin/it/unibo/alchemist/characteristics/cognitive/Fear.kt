package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.characteristics.utils.Functions

class Fear : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
        maxOf(wPersisting * level,
                Functions.advancedLogistic(aLogisticSigma, aLogisticTau,
                        aggregateFears(),
                        wAmplifyingFeeling * owner.desireEvacuateLevel(),
                        wInhibitingFeeling * owner.desireWalkRandomlyLevel()))

    private fun aggregateFears() = with(owner.influencialPeople()) {
        this.sumByDouble { it.fearLevel() } / this.size
    }
}