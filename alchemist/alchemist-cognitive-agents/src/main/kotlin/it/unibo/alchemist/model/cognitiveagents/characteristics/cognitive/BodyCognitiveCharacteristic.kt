package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

/**
 * A cognitive characteristic which has a body response.
 */
abstract class BodyCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currentLevel += bodyEta * combinationFunction() * deltaT
    }
}