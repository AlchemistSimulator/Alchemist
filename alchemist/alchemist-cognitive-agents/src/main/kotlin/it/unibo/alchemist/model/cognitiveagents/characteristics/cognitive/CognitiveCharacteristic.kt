package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import it.unibo.alchemist.model.cognitiveagents.characteristics.Characteristic

/**
 * A characteristic which depends also on the other pedestrians in the environment.
 */
interface CognitiveCharacteristic : Characteristic {

    /**
     * A number between 0 and 1 describing the current intensity of this characteristic.
     */
    fun level(): Double

    /**
     * Update the current intensity of this characteristic.
     */
    fun update(deltaT: Double)
}