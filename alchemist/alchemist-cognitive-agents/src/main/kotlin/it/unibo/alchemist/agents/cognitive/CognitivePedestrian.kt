package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian
import it.unibo.alchemist.characteristics.cognitive.CognitiveCharacteristic

/**
 * An heterogeneous pedestrian with cognitive capabilities too.
 */
interface CognitivePedestrian<T> : HeterogeneousPedestrian<T> {

    fun dangerBelief(): Double

    fun fear(): Double

    fun influencialPeople(): Collection<CognitivePedestrian<*>>

    fun cognitiveCharacteristics(): Collection<CognitiveCharacteristic>
}