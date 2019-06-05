package it.unibo.alchemist.characteristics.cognitive

abstract class BodyCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currentLevel += bodyEta * combinationFunction() * deltaT
    }
}