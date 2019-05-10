package it.unibo.alchemist.characteristics.cognitive

class DesireEvacuate : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
            wComplying * maxOf(wAmplifyingEvacuation * owner.dangerBeliefLevel(), wAmplifyingEvacuation * owner.fearLevel())
}