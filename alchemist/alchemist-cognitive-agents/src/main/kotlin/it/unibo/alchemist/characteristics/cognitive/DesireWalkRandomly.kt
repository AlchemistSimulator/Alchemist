package it.unibo.alchemist.characteristics.cognitive

class DesireWalkRandomly : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
            wNonComplying * (1 - maxOf(wInhibitingWalkRand * owner.dangerBeliefLevel(), wInhibitingWalkRand * owner.fearLevel()))
}