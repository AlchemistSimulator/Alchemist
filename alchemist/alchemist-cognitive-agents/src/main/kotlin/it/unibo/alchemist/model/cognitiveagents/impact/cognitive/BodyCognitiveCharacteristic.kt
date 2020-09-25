package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

/**
 * A cognitive characteristic which has a body response.
 */
abstract class BodyCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(frequency: Double) {
        currentLevel += bodyEta * combinationFunction() * frequency
    }
}
