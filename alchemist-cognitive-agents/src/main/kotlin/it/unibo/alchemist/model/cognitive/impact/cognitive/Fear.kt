package it.unibo.alchemist.model.cognitive.impact.cognitive

import it.unibo.alchemist.model.cognitive.CognitiveModel
import it.unibo.alchemist.util.math.advancedLogistic

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
    private val influencialPeople: () -> List<CognitiveModel>,
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() = maxOf(
        persistingOmega * currentLevel,
        advancedLogistic(
            advancedLogisticSigma,
            advancedLogisticTau,
            influencialPeople().aggregateFears(),
            amplifyingFeelingOmega * desireEvacuate(),
            inhibitingFeelingOmega * desireWalkRandomly(),
        ),
    )

    private fun List<CognitiveModel>.aggregateFears() =
        if (isEmpty()) 0.0 else sumOf { it.fear() } / this.size
}
