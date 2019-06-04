package it.unibo.alchemist.characteristics.cognitive

class DesireWalkRandomly(
    private val compliance: Double,
    private val dangerBelief: () -> Double,
    private val fear: () -> Double
) : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
        compliance * (1 - maxOf(wInhibitingWalkRand * dangerBelief(), wInhibitingWalkRand * fear()))
}