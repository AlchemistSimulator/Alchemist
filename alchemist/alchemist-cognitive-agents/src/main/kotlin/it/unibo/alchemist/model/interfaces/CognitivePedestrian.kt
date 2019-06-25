package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.CognitiveCharacteristic

/**
 * An heterogeneous pedestrian with cognitive capabilities too.
 */
interface CognitivePedestrian<T> : HeterogeneousPedestrian<T> {

    /**
     * Value between 0 and 1 representing the current belief of the situation dangerousness for this pedestrian.
     */
    fun dangerBelief(): Double

    /**
     * Value between 0 and 1 representing the level of fear of this pedestrian.
     */
    fun fear(): Double

    /**
     * A collection of all the pedestrians in the same environment who have an influence on this pedestrian.
     */
    fun influencialPeople(): Collection<CognitivePedestrian<*>>

    /**
     * The list of all the cognitive characteristics of this pedestrian.
     */
    fun cognitiveCharacteristics(): Collection<CognitiveCharacteristic>
}