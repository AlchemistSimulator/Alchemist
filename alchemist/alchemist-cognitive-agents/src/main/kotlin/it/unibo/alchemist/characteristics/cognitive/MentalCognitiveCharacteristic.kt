package it.unibo.alchemist.characteristics.cognitive

abstract class MentalCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currentLevel += mentalEta * (combinationFunction() - currentLevel) * deltaT
    }
}