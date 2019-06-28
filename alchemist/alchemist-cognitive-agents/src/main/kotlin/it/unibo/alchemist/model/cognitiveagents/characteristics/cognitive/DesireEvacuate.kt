package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

class DesireEvacuate(
    private val compliance: Double,
    private val dangerBelief: () -> Double,
    private val fear: () -> Double
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
        compliance * maxOf(amplifyingEvacuationOmega * dangerBelief(), amplifyingEvacuationOmega * fear())
}