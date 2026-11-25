package it.unibo.alchemist.model.cognitive.impact.cognitive

import it.unibo.alchemist.model.cognitive.impact.ImpactParameters
import it.unibo.alchemist.util.math.advancedLogistic
import it.unibo.alchemist.util.math.logistic

/**
 * The generic implementation of a cognitive characteristic.
 */
abstract class AbstractCognitiveCharacteristic : CognitiveCharacteristic {
    /**
     * The current level of this characteristic.
     */
    protected var currentLevel: Double = 0.0

    override fun level() = currentLevel

    /**
     * Algorithm which decides how the parameters involved
     * in the evolution of this characteristic must be combined together.
     * It can be either a max, min, summation or any other type of function.
     */
    abstract fun combinationFunction(): Double

    /**
     * Cognitive characteristics are modeled following the principles of
     * [Network Oriented Modeling](https://doi.org/10.1007/978-3-662-58611-2_2), which allow
     * characteristics to influence each other and evolve during the simulation. Each characteristic
     * is modeled as an equation; weights and constant values used in equations are defined below.
     * These are described in the [IMPACT model](https://doi.org/10.1007/978-3-319-70647-4_11).
     */
    companion object {
        private val parameters = ImpactParameters.default.cognitive

        /**
         * Capacity of sensing the danger.
         */
        val sensingOmega = parameters.sensingOmega

        /**
         * Influence of fear on danger belief.
         */
        val affectiveBiasingOmega = parameters.affectiveBiasingOmega

        /**
         * Persistence of emotions.
         */
        val persistingOmega = parameters.persistingOmega

        /**
         * Amplifies fear sensation.
         */
        val amplifyingFeelingOmega = parameters.amplifyingFeelingOmega

        /**
         * Inhibits fear sensation.
         */
        val inhibitingFeelingOmega = parameters.inhibitingFeelingOmega

        /**
         * Amplifies the desire to evacuate.
         */
        val amplifyingEvacuationOmega = parameters.amplifyingEvacuationOmega

        /**
         * Inhibits the desire to evacuate.
         */
        val inhibitingWalkRandOmega = parameters.inhibitingWalkRandOmega

        /**
         * Amplifies the intention to evacuate.
         */
        val amplifyingIntentionOmega = parameters.amplifyingIntentionOmega

        /**
         * Inhibits the intention to evacuate.
         */
        val inhibitingIntentionOmega = parameters.inhibitingIntentionOmega

        /**
         * Intensity of human mental response.
         */
        val mentalEta = parameters.mentalEta

        /**
         * Intensity of human body response.
         */
        val bodyEta = parameters.bodyEta

        /**
         * Sigma of the [logistic] function used to compute [IntentionEvacuate] and [IntentionWalkRandomly].
         */
        val logisticSigma = parameters.logisticSigma

        /**
         * Tau of the [logistic] function used to compute [IntentionEvacuate] and [IntentionWalkRandomly].
         */
        val logisticTau = parameters.logisticTau

        /**
         * Sigma of the [advancedLogistic] function used to compute [Fear].
         */
        val advancedLogisticSigma = parameters.advancedLogisticSigma

        /**
         * Tau of the [advancedLogistic] function used to compute [Fear].
         */
        val advancedLogisticTau = parameters.advancedLogisticTau
    }
}
