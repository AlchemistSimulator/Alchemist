package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

/**
 * A cognitive characteristic which has a mental response.
 */
abstract class MentalCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currentLevel += mentalEta * (combinationFunction() - currentLevel) * deltaT
    }
}