package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.CognitiveCharacteristic
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.BeliefDanger
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.Fear
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.DesireWalkRandomly
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.DesireEvacuate
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.IntentionEvacuate
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.IntentionWalkRandomly
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KClass

abstract class AbstractCognitivePedestrian<T, P : Position<P>> (
    protected open val env: Environment<T, P>,
    protected val rg: RandomGenerator,
    age: Age,
    gender: Gender,
    danger: Molecule?
) : AbstractHeterogeneousPedestrian<T>(env, rg, age, gender), CognitivePedestrian<T> {

    private val dangerousLayerLevel: () -> Double = {
        env.getLayer(danger).let { if (it.isPresent) it.get().getValue(env.getPosition(this)) as Double else 0.0 }
    }

    private val cognitiveCharacteristics = linkedMapOf<KClass<out CognitiveCharacteristic>, CognitiveCharacteristic>(
        BeliefDanger::class to
            BeliefDanger(dangerousLayerLevel, { characteristicLevel<Fear>() }, { influencialPeople() }),
        Fear::class to
            Fear({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() }, { influencialPeople() }),
        DesireEvacuate::class to
            DesireEvacuate(compliance, { characteristicLevel<BeliefDanger>() }, { characteristicLevel<Fear>() }),
        DesireWalkRandomly::class to
            DesireWalkRandomly(compliance, { characteristicLevel<BeliefDanger>() }, { characteristicLevel<Fear>() }),
        IntentionEvacuate::class to
            IntentionEvacuate({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() }),
        IntentionWalkRandomly::class to
            IntentionWalkRandomly({ characteristicLevel<DesireWalkRandomly>() }, { characteristicLevel<DesireEvacuate>() })
    )

    override fun dangerBelief() = characteristicLevel<BeliefDanger>()

    override fun fear() = characteristicLevel<Fear>()

    override fun cognitiveCharacteristics() = cognitiveCharacteristics.values

    private inline fun <reified C : CognitiveCharacteristic> characteristicLevel(): Double =
        cognitiveCharacteristics[C::class]?.level() ?: 0.0
}