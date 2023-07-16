package it.unibo.alchemist.model.cognitive.impact.cognitive

/**
 * A cognitive characteristic which has a mental response.
 */
abstract class MentalCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(frequency: Double) {
        currentLevel += mentalEta * (combinationFunction() - currentLevel) * frequency
    }
}
