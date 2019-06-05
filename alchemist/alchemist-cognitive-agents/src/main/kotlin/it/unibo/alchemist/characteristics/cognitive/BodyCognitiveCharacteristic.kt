package it.unibo.alchemist.characteristics.cognitive

abstract class BodyCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currLevel += nBody * combinationFunction() * deltaT
    }
}