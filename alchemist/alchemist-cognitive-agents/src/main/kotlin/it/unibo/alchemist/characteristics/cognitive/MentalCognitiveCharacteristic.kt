package it.unibo.alchemist.characteristics.cognitive

abstract class MentalCognitiveCharacteristic : CognitiveCharacteristic() {

    var level: Double = 0.0
        private set

    fun update() {
        level += nMental * (combinationFunction() - level) * deltaT
    }
}