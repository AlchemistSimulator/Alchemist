package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import it.unibo.alchemist.model.cognitiveagents.characteristics.Characteristic

/**
 * A characteristic which depends on the other agents in the environment.
 */
interface CognitiveCharacteristic : Characteristic {

    /**
     * The current intensity of this characteristic.
     */
    fun level(): Double

    /**
     * Update the current intensity of this characteristic.
     */
    fun update(deltaT: Double)
}
