package it.unibo.alchemist.characteristics.cognitive

abstract class MentalCognitiveCharacteristic : AbstractCognitiveCharacteristic() {

    override fun update(deltaT: Double) {
        currLevel += nMental * (combinationFunction() - currLevel) * deltaT
    }
}