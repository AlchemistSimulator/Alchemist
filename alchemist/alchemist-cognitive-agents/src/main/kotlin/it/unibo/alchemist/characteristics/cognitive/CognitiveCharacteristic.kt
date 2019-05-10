package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.agents.cognitive.CognitivePedestrian
import it.unibo.alchemist.characteristics.Characteristic

/**
 * A characteristic which depends also on the other pedestrians in the environment
 */
abstract class CognitiveCharacteristic : Characteristic {

    protected lateinit var owner: CognitivePedestrian<*>

    fun ownership(ped: CognitivePedestrian<*>) { owner = ped }

    companion object {
        val wSensing = 0.5
        val wAffectiveBiasing = 1.0
        val wPersisting = 0.95
        val wComplying = 1.0
        val wNonComplying = 1.0
        val wAmplifyingFeeling = 1.0
        val wInhibitingFeeling = 1.0
        val wAmplifyingEvacuation = 1.0
        val wInhibitingWalkRand = -1.0
        val wAmplifyingIntention = 1.0
        val wInhibitingIntention = -1.0
        val nMental = 0.9
        val nBody = 0.25
        val logisticSigma = 20.0
        val logisticTau = 0.5
        val aLogisticSigma = 2.0
        val aLogisticTau = 0.14
        val deltaT = 1.0
    }

    protected abstract fun combinationFunction(): Double
}