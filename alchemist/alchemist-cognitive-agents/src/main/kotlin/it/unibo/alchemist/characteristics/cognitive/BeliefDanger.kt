package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.agents.cognitive.CognitivePedestrian

class BeliefDanger(
    private val fear: () -> Double,
    private val influencialPeople: () -> Collection<CognitivePedestrian<*>>
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
            maxOf(wPersisting * level(),
                    (wAffectiveBiasing * fear() + influencialPeople().aggregateDangerBeliefs()) / (wAffectiveBiasing + 1))

    private fun Collection<CognitivePedestrian<*>>.aggregateDangerBeliefs() =
            this.sumByDouble { it.dangerBelief() } / this.size
}