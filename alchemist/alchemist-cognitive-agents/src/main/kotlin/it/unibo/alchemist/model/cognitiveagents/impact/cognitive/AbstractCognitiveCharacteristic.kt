package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import it.unibo.alchemist.model.cognitiveagents.impact.PARAMETERS_FILE
import it.unibo.alchemist.model.cognitiveagents.impact.cognitive.utils.logistic
import it.unibo.alchemist.model.cognitiveagents.impact.cognitive.utils.advancedLogistic

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
     * [Network Oriented Modeling](https://doi. org/10.1007/978-3-662-58611-2_2), which allows
     * characteristics to influence each other and evolve during the simulation. Each characteristic
     * is modeled as an equation; weights and constant values used in equations are defined below.
     * These are described in the [IMPACT model](https://doi.org/10.1007/978-3-319-70647-4_11).
     */
    companion object {
        private val config = Config { addSpec(CognitiveSpec) }
            .from.toml.resource(PARAMETERS_FILE)
        /**
         * Capacity of sensing the danger.
         */
        val sensingOmega = config[CognitiveSpec.sensingOmega]
        /**
         * Influence of fear on danger belief.
         */
        val affectiveBiasingOmega = config[CognitiveSpec.affectiveBiasingOmega]
        /**
         * Persistence of emotions.
         */
        val persistingOmega = config[CognitiveSpec.persistingOmega]
        /**
         * Amplifies fear sensation.
         */
        val amplifyingFeelingOmega = config[CognitiveSpec.amplifyingFeelingOmega]
        /**
         * Inhibits fear sensation.
         */
        val inhibitingFeelingOmega = config[CognitiveSpec.inhibitingFeelingOmega]
        /**
         * Amplifies the desire to evacuate.
         */
        val amplifyingEvacuationOmega = config[CognitiveSpec.amplifyingEvacuationOmega]
        /**
         * Inhibits the desire to evacuate.
         */
        val inhibitingWalkRandOmega = config[CognitiveSpec.inhibitingWalkRandOmega]
        /**
         * Amplifies the intention to evacuate.
         */
        val amplifyingIntentionOmega = config[CognitiveSpec.amplifyingIntentionOmega]
        /**
         * Inhibits the intention to evacuate.
         */
        val inhibitingIntentionOmega = config[CognitiveSpec.inhibitingIntentionOmega]
        /**
         * Intensity of human mental response.
         */
        val mentalEta = config[CognitiveSpec.mentalEta]
        /**
         * Intensity of human body response.
         */
        val bodyEta = config[CognitiveSpec.bodyEta]
        /**
         * Sigma of the [logistic] function used to compute [IntentionEvacuate] and [IntentionWalkRandomly].
         */
        val logisticSigma = config[CognitiveSpec.logisticSigma]
        /**
         * Tau of the [logistic] function used to compute [IntentionEvacuate] and [IntentionWalkRandomly].
         */
        val logisticTau = config[CognitiveSpec.logisticTau]
        /**
         * Sigma of the [advancedLogistic] function used to compute [Fear].
         */
        val advancedLogisticSigma = config[CognitiveSpec.advancedLogisticSigma]
        /**
         * Tau of the [advancedLogistic] function used to compute [Fear].
         */
        val advancedLogisticTau = config[CognitiveSpec.advancedLogisticTau]
    }
}
