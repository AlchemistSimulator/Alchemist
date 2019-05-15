package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.AbstractHeterogeneousPedestrian
import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian
import it.unibo.alchemist.characteristics.cognitive.*
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import kotlin.reflect.KClass

abstract class AbstractCognitivePedestrian<T, P : Position<P>> (
    env: Environment<T, P>,
    age: Age,
    gender: Gender
) : CognitivePedestrian<T>, AbstractHeterogeneousPedestrian<T>(env, age, gender) {

    private val cognitiveCharacteristics: Map<KClass<out CognitiveCharacteristic>, CognitiveCharacteristic> =
        mapOf(
            BeliefDanger::class to BeliefDanger({ characteristicLevel<Fear>() }, { influencialPeople() }),
            Fear::class to Fear({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() }, { influencialPeople() }),
            DesireEvacuate::class to DesireEvacuate({ characteristicLevel<BeliefDanger>() }, { characteristicLevel<Fear>() }),
            DesireWalkRandomly::class to DesireWalkRandomly({ characteristicLevel<BeliefDanger>() }, { characteristicLevel<Fear>() }),
            IntentionEvacuate::class to IntentionEvacuate({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() }),
            IntentionWalkRandomly::class to IntentionWalkRandomly({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() })
        )

    override fun dangerBelief() = characteristicLevel<BeliefDanger>()

    override fun fear() = characteristicLevel<Fear>()

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>): Double = 0.0
        //toHelp: HeterogeneousPedestrian<T> -> characteristicLevel(HelpAttitude::class).level(toHelp)

    private inline fun <reified C : CognitiveCharacteristic> characteristicLevel(): Double =
        cognitiveCharacteristics[C::class]?.level() ?: 0.0
}