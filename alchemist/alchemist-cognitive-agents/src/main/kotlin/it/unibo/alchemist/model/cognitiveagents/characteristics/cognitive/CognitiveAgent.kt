package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

/**
 * An entity capable of having emotions and relations.
 */
interface CognitiveAgent {

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
    fun influencialPeople(): List<CognitiveAgent>
}