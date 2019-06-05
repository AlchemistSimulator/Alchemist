package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.agents.cognitive.CognitivePedestrian
import it.unibo.alchemist.characteristics.utils.advancedLogistic

class Fear(
    private val desireWalkRandomly: () -> Double,
    private val desireEvacuate: () -> Double,
    private val influencialPeople: () -> Collection<CognitivePedestrian<*>>
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() = maxOf(
        persistingOmega * currentLevel,
        advancedLogistic(
            advancedLogisticSigma, advancedLogisticTau,
            influencialPeople().aggregateFears(),
            amplifyingFeelingOmega * desireEvacuate(),
            inhibitingFeelingOmega * desireWalkRandomly()
        )
    )

    private fun Collection<CognitivePedestrian<*>>.aggregateFears() =
        this.sumByDouble { it.fear() } / this.size
}