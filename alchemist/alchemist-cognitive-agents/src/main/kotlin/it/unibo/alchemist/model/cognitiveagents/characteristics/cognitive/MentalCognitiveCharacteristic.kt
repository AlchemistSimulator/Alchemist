package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

abstract class MentalCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currentLevel += mentalEta * (combinationFunction() - currentLevel) * deltaT
    }
}