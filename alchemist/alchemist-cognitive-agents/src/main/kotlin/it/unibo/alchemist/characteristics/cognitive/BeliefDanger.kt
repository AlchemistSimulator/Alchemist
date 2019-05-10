package it.unibo.alchemist.characteristics.cognitive

class BeliefDanger : MentalCognitiveCharacteristic() {

    override fun combinationFunction() =
            maxOf(wPersisting * level,
                    (wAffectiveBiasing * owner.fearLevel() + aggregateBeliefs()) / (wAffectiveBiasing + 1))

    private fun aggregateBeliefs() = with(owner.influencialPeople()) {
        this.sumByDouble { it.dangerBeliefLevel() } / this.size
    }
}