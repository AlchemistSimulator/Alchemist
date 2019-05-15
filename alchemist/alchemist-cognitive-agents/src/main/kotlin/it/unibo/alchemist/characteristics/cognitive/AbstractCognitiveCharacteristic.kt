package it.unibo.alchemist.characteristics.cognitive

abstract class AbstractCognitiveCharacteristic : CognitiveCharacteristic {

    protected var currLevel: Double = 0.0

    override fun level() = currLevel

    abstract fun combinationFunction(): Double

    // must be put in a configuration file
    companion object {
        const val wAffectiveBiasing = 1.0
        const val wPersisting = 0.95
        const val wComplying = 1.0
        const val wNonComplying = 1.0
        const val wAmplifyingFeeling = 1.0
        const val wInhibitingFeeling = 1.0
        const val wAmplifyingEvacuation = 1.0
        const val wInhibitingWalkRand = -1.0
        const val wAmplifyingIntention = 1.0
        const val wInhibitingIntention = -1.0
        const val nMental = 0.9
        const val nBody = 0.25
        const val logisticSigma = 20.0
        const val logisticTau = 0.5
        const val aLogisticSigma = 2.0
        const val aLogisticTau = 0.14
    }
}