package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.CognitiveAgent
import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.cognitiveagents.impact.ImpactModel
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator

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
    group: PedestrianGroup<T, P, A>? = null,
    cognitive: CognitiveModel? = null
) : AbstractHeterogeneousPedestrian<T, P, A, F>(environment, randomGenerator, age, gender, group),
    CognitivePedestrian<T, P, A>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    override val cognitive by lazy {
        cognitive ?: ImpactModel(this, compliance) {
            environment.getLayer(danger)
                .map { it.getValue(environment.getPosition(this)) as Double }
                .orElse(0.0)
        }
    }

    override fun speed() =
        if (wantsToEscape()) {
            runningSpeed * minOf(cognitive.escapeIntention(), 1.0)
        } else {
            walkingSpeed * minOf(cognitive.remainIntention(), 1.0)
        }

    override fun influencialPeople(): List<CognitiveAgent> =
        senses.fold(listOf()) { accumulator, sphere ->
            accumulator.union(sphere.influentialNodes().filterIsInstance<CognitiveAgent>()).toList()
        }
}
