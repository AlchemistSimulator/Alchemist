package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.cognitiveagents.impact.ImpactModel
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
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
    backingNode: Node<T>,
    age: Age,
    gender: Gender,
    val danger: Molecule? = null,
    group: PedestrianGroup<T, P, A>? = null,
    cognitive: CognitiveModel? = null
) : AbstractHeterogeneousPedestrian<T, P, A, F>(randomGenerator, backingNode, age, gender, group),
    CognitivePedestrian<T, P, A>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    override val cognitiveModel: CognitiveModel by lazy {
        cognitive ?: ImpactModel(pedestrianModel.compliance, ::influencialPeople) {
            environment.getLayer(danger)
                .map { it.getValue(environment.getPosition(this)) as Double }
                .orElse(0.0)
        }
    }

    override fun speed() =
        if (cognitiveModel.wantsToEscape()) {
            runningSpeed * minOf(cognitiveModel.escapeIntention(), 1.0)
        } else {
            walkingSpeed * minOf(cognitiveModel.remainIntention(), 1.0)
        }
}
