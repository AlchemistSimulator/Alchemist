package it.unibo.alchemist.characteristics.cognitive

abstract class BodyCognitiveCharacteristic : CognitiveCharacteristic() {

    var level: Double = 0.0
        private set

    fun update() {
        level += nBody * combinationFunction() * deltaT
    }
}