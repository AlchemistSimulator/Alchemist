package it.unibo.alchemist.characteristics.cognitive

class DesireEvacuate(
    private val dangerBelief: () -> Double,
    private val fear: () -> Double
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
            wComplying * maxOf(wAmplifyingEvacuation * dangerBelief(), wAmplifyingEvacuation * fear())
}