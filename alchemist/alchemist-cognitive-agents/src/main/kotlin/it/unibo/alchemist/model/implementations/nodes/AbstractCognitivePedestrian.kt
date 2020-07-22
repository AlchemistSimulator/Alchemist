package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.BeliefDanger
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.CognitiveAgent
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.CognitiveCharacteristic
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.DesireEvacuate
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.DesireWalkRandomly
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.Fear
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.IntentionEvacuate
import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.IntentionWalkRandomly
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KClass

/**
 * Implementation of a cognitive pedestrian.
 *
 * @param environment
 *          the environment inside which this pedestrian moves.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 * @param danger
 *          the molecule associated to danger in the environment.
 */
abstract class AbstractCognitivePedestrian<T, P, A, F> @JvmOverloads constructor(
    environment: PhysicsEnvironment<T, P, A, F>,
    randomGenerator: RandomGenerator,
    age: Age,
    gender: Gender,
    val danger: Molecule? = null,
    group: PedestrianGroup<T, P, A>? = null
) : AbstractHeterogeneousPedestrian<T, P, A, F>(environment, randomGenerator, age, gender, group),
    CognitivePedestrian<T, P, A>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    private val cognitiveCharacteristics = linkedMapOf<KClass<out CognitiveCharacteristic>, CognitiveCharacteristic>(
        BeliefDanger::class to
            BeliefDanger({ dangerousLayerLevel() }, { characteristicLevel<Fear>() }, { influencialPeople() }),
        Fear::class to Fear(
            { characteristicLevel<DesireWalkRandomly>() },
            { characteristicLevel<DesireEvacuate>() },
            { influencialPeople() }
        ),
        DesireEvacuate::class to DesireEvacuate(
            compliance,
            { characteristicLevel<BeliefDanger>() },
            { characteristicLevel<Fear>() }
        ),
        DesireWalkRandomly::class to DesireWalkRandomly(
            compliance,
            { characteristicLevel<BeliefDanger>() },
            { characteristicLevel<Fear>() }
        ),
        IntentionEvacuate::class to IntentionEvacuate(
            { characteristicLevel<DesireWalkRandomly>() },
            { characteristicLevel<DesireEvacuate>() }
        ),
        IntentionWalkRandomly::class to IntentionWalkRandomly(
            { characteristicLevel<DesireWalkRandomly>() },
            { characteristicLevel<DesireEvacuate>() }
        )
    )

    override fun speed() =
        if (wantsToEvacuate()) {
            runningSpeed * minOf(characteristicLevel<IntentionEvacuate>(), 1.0)
        } else {
            walkingSpeed * minOf(characteristicLevel<IntentionWalkRandomly>(), 1.0)
        }

    override fun dangerBelief() = characteristicLevel<BeliefDanger>()

    override fun fear() = characteristicLevel<Fear>()

    override fun cognitiveCharacteristics() = cognitiveCharacteristics.values.toList()

    private inline fun <reified C : CognitiveCharacteristic> characteristicLevel(): Double =
        cognitiveCharacteristics[C::class]?.level() ?: 0.0

    private fun dangerousLayerLevel(): Double =
        environment.getLayer(danger)
            .map { it.getValue(environment.getPosition(this)) as Double }
            .orElse(0.0)

    override fun wantsToEvacuate(): Boolean =
        characteristicLevel<IntentionEvacuate>() > characteristicLevel<IntentionWalkRandomly>()

    override fun influencialPeople(): List<CognitiveAgent> =
        senses.fold(listOf()) { accumulator, sphere ->
            accumulator.union(sphere.influentialNodes().filterIsInstance<CognitiveAgent>()).toList()
        }
}
