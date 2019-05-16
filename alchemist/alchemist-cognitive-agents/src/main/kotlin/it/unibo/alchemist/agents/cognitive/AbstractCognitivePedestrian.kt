package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.AbstractHeterogeneousPedestrian
import it.unibo.alchemist.characteristics.cognitive.CognitiveCharacteristic
import it.unibo.alchemist.characteristics.cognitive.BeliefDanger
import it.unibo.alchemist.characteristics.cognitive.Fear
import it.unibo.alchemist.characteristics.cognitive.DesireWalkRandomly
import it.unibo.alchemist.characteristics.cognitive.DesireEvacuate
import it.unibo.alchemist.characteristics.cognitive.IntentionEvacuate
import it.unibo.alchemist.characteristics.cognitive.IntentionWalkRandomly
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KClass

abstract class AbstractCognitivePedestrian<T, P : Position<P>> (
    env: Environment<T, P>,
    rg: RandomGenerator,
    age: String,
    gender: String
) : CognitivePedestrian<T>, AbstractHeterogeneousPedestrian<T>(env, rg, age, gender) {

    private val cognitiveCharacteristics: Map<KClass<out CognitiveCharacteristic>, CognitiveCharacteristic> = mapOf(
        BeliefDanger::class to BeliefDanger({ characteristicLevel<Fear>() }, { influencialPeople() }),
        Fear::class to Fear({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() }, { influencialPeople() }),
        DesireEvacuate::class to DesireEvacuate({ characteristicLevel<BeliefDanger>() }, { characteristicLevel<Fear>() }),
        DesireWalkRandomly::class to DesireWalkRandomly({ characteristicLevel<BeliefDanger>() }, { characteristicLevel<Fear>() }),
        IntentionEvacuate::class to IntentionEvacuate({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() }),
        IntentionWalkRandomly::class to IntentionWalkRandomly({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() })
    )

    override fun dangerBelief() = characteristicLevel<BeliefDanger>()

    override fun fear() = characteristicLevel<Fear>()

    override fun cognitiveCharacteristics() = cognitiveCharacteristics.values

    private inline fun <reified C : CognitiveCharacteristic> characteristicLevel(): Double =
        cognitiveCharacteristics[C::class]?.level() ?: 0.0
}