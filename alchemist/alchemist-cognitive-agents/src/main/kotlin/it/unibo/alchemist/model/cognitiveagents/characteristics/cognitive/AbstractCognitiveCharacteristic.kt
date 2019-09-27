package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import com.uchuhimo.konf.Config
import it.unibo.alchemist.model.cognitiveagents.characteristics.PARAMETERS_FILE

abstract class AbstractCognitiveCharacteristic : CognitiveCharacteristic {

    protected var currentLevel: Double = 0.0

    override fun level() = currentLevel

    abstract fun combinationFunction(): Double

    companion object {
        private val config = Config { addSpec(CognitiveSpec) }
            .from.toml.resource(PARAMETERS_FILE)

        val sensingOmega = config[CognitiveSpec.sensingOmega]
        val affectiveBiasingOmega = config[CognitiveSpec.affectiveBiasingOmega]
        val persistingOmega = config[CognitiveSpec.persistingOmega]
        val amplifyingFeelingOmega = config[CognitiveSpec.amplifyingFeelingOmega]
        val inhibitingFeelingOmega = config[CognitiveSpec.inhibitingFeelingOmega]
        val amplifyingEvacuationOmega = config[CognitiveSpec.amplifyingEvacuationOmega]
        val inhibitingWalkRandOmega = config[CognitiveSpec.inhibitingWalkRandOmega]
        val amplifyingIntentionOmega = config[CognitiveSpec.amplifyingIntentionOmega]
        val inhibitingIntentionOmega = config[CognitiveSpec.inhibitingIntentionOmega]
        val mentalEta = config[CognitiveSpec.mentalEta]
        val bodyEta = config[CognitiveSpec.bodyEta]
        val logisticSigma = config[CognitiveSpec.logisticSigma]
        val logisticTau = config[CognitiveSpec.logisticTau]
        val advancedLogisticSigma = config[CognitiveSpec.advancedLogisticSigma]
        val advancedLogisticTau = config[CognitiveSpec.advancedLogisticTau]
    }
}
