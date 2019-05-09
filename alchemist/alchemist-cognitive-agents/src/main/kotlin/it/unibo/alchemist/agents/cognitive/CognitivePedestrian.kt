package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian

/**
 * An heterogeneous pedestrian with cognitive capabilities too.
 */
interface CognitivePedestrian<T> : HeterogeneousPedestrian<T> {

    val dangerBeliefLevel: () -> Double

    val fearLevel: () -> Double

    val influencialPeople: () -> Collection<CognitivePedestrian<*>>

    val probabilityOfHelping: (toHelp: HeterogeneousPedestrian<T>) -> Double
}