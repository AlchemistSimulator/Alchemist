package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import it.unibo.alchemist.model.interfaces.CognitivePedestrian

class BeliefDanger(
    private val dangerousZone: () -> Double,
    private val fear: () -> Double,
    private val influencialPeople: () -> List<CognitivePedestrian<*>>
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() = maxOf(
        sensingOmega * dangerousZone(),
        persistingOmega * level(),
        (affectiveBiasingOmega * fear() + influencialPeople().aggregateDangerBeliefs()) / (affectiveBiasingOmega + 1)
    )

    private fun List<CognitivePedestrian<*>>.aggregateDangerBeliefs() =
        this.sumByDouble { it.dangerBelief() } / this.size
}