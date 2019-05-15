package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian

/**
 * An heterogeneous pedestrian with cognitive capabilities too.
 */
interface CognitivePedestrian<T> : HeterogeneousPedestrian<T> {

    fun dangerBelief(): Double

    fun fear(): Double

    fun influencialPeople(): Collection<CognitivePedestrian<*>>

    fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>): Double
}