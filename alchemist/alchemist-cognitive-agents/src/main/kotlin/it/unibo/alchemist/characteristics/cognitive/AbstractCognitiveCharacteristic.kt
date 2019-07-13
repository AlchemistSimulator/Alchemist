package it.unibo.alchemist.characteristics.cognitive

abstract class AbstractCognitiveCharacteristic : CognitiveCharacteristic {

    protected var currentLevel: Double = 0.0

    override fun level() = currentLevel

    abstract fun combinationFunction(): Double

    // TODO: These values must be set inside a configuration file
    companion object {
        const val sensingOmega = 0.5
        const val affectiveBiasingOmega = 1.0
        const val persistingOmega = 0.95
        const val amplifyingFeelingOmega = 1.0
        const val inhibitingFeelingOmega = 1.0
        const val amplifyingEvacuationOmega = 1.0
        const val inhibitingWalkRandOmega = -1.0
        const val amplifyingIntentionOmega = 1.0
        const val inhibitingIntentionOmega = -1.0
        const val mentalEta = 0.9
        const val bodyEta = 0.25
        const val logisticSigma = 20.0
        const val logisticTau = 0.5
        const val advancedLogisticSigma = 2.0
        const val advancedLogisticTau = 0.14
    }
}