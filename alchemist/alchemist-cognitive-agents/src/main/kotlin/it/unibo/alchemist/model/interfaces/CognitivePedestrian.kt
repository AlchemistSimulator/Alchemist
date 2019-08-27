package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.CognitiveCharacteristic

/**
 * An heterogeneous pedestrian with cognitive capabilities too.
 */
interface CognitivePedestrian<T> : HeterogeneousPedestrian<T> {

    /**
     * Value representing the current belief of the situation dangerousness for this pedestrian.
     */
    fun dangerBelief(): Double

    /**
     * Value representing the level of fear of this pedestrian.
     */
    fun fear(): Double

    /**
     * Whether or not this pedestrian intends to evacuate.
     */
    fun wantsToEvacuate(): Boolean

    /**
     * The list of all the cognitive characteristics of this pedestrian.
     */
    fun cognitiveCharacteristics(): List<CognitiveCharacteristic>

    /**
     * A list of all the pedestrians inside at least one of the sensory spheres of this pedestrian.
     */
    fun influencialPeople(): List<CognitivePedestrian<T>>
}