package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

import it.unibo.alchemist.meanOrZero
import it.unibo.alchemist.model.cognitiveagents.CognitiveAgent
import it.unibo.alchemist.model.cognitiveagents.impact.cognitive.utils.advancedLogistic

/**
 * The fear emotion.
 *
 * @param desireWalkRandomly
 *          the current desire of not evacuating of the agent owning this.
 * @param desireEvacuate
 *          the current desire of evacuating of the agent owning this.
 * @param influencialPeople
 *          the list of cognitive agents with an influence on the agent owning this.
 */
class Fear(
    private val desireWalkRandomly: () -> Double,
    private val desireEvacuate: () -> Double,
    private val influencialPeople: () -> List<CognitiveAgent>
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() = maxOf(
        persistingOmega * currentLevel,
        advancedLogistic(
            advancedLogisticSigma,
            advancedLogisticTau,
            influencialPeople().aggregateFears(),
            amplifyingFeelingOmega * desireEvacuate(),
            inhibitingFeelingOmega * desireWalkRandomly()
        )
    )

    private fun List<CognitiveAgent>.aggregateFears() = meanOrZero { cognitive.fear() }
}
