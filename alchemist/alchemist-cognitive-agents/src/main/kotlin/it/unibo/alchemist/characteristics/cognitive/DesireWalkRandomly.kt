package it.unibo.alchemist.characteristics.cognitive

class DesireWalkRandomly(
    private val dangerBelief: () -> Double,
    private val fear: () -> Double
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
            wNonComplying * (1 - maxOf(wInhibitingWalkRand * dangerBelief(), wInhibitingWalkRand * fear()))
}