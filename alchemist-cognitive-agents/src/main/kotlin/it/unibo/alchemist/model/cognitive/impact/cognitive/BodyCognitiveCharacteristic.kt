package it.unibo.alchemist.model.cognitive.impact.cognitive

/**
 * A cognitive characteristic which has a body response.
 */
abstract class BodyCognitiveCharacteristic : AbstractCognitiveCharacteristic() {
    override fun update(timeStep: Double) {
        currentLevel += bodyEta * combinationFunction() * timeStep
    }
}
