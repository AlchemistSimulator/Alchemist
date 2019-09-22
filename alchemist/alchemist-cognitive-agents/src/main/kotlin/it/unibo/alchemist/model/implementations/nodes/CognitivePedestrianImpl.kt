package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.*
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.*
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KClass

/**
 * Implementation of a cognitive pedestrian.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 * @param danger
 *          the molecule associated to danger in the environment.
 */
open class CognitivePedestrianImpl<T, P : Position<P>> @JvmOverloads constructor(
    private val env: Environment<T, P>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender,
    private val danger: Molecule? = null,
    group: PedestrianGroup<T>? = null
) : HeterogeneousPedestrianImpl<T, P>(env, rg, age, gender, group), CognitivePedestrian<T> {

    private val cognitiveCharacteristics = linkedMapOf<KClass<out CognitiveCharacteristic>, CognitiveCharacteristic>(
        BeliefDanger::class to
            BeliefDanger({ dangerousLayerLevel() }, { characteristicLevel<Fear>() }, { influencialPeople() }),
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

    override fun speed() =
        if (wantsToEvacuate())
            runningSpeed * minOf(characteristicLevel<IntentionEvacuate>(), 1.0)
        else
            walkingSpeed * minOf(characteristicLevel<IntentionWalkRandomly>(), 1.0)

    override fun dangerBelief() = characteristicLevel<BeliefDanger>()

    override fun fear() = characteristicLevel<Fear>()

    override fun cognitiveCharacteristics() = cognitiveCharacteristics.values.toList()

    private inline fun <reified C : CognitiveCharacteristic> characteristicLevel(): Double =
        cognitiveCharacteristics[C::class]?.level() ?: 0.0

    private fun dangerousLayerLevel(): Double =
        env.getLayer(danger).let { if (it.isPresent) it.get().getValue(env.getPosition(this)) as Double else 0.0 }

    override fun wantsToEvacuate(): Boolean =
        characteristicLevel<IntentionEvacuate>() > characteristicLevel<IntentionWalkRandomly>()

    override fun influencialPeople(): List<CognitiveAgent> =
        senses.fold(listOf()) { accumulator, sphere ->
            accumulator.union(sphere.influentialNodes().filterIsInstance<CognitiveAgent>()).toList()
        }
}