package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Compliance
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.impact.individual.HelpAttitude
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a heterogeneous pedestrian.
 *
 * @param environment
 *          the environment inside which this pedestrian moves.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 */
abstract class AbstractHeterogeneousPedestrian<T, P, A, F> @JvmOverloads constructor(
    environment: PhysicsEnvironment<T, P, A, F>,
    randomGenerator: RandomGenerator,
    final override val age: Age,
    final override val gender: Gender,
    group: PedestrianGroup<T, P, A>? = null
) : AbstractHomogeneousPedestrian<T, P, A, F>(environment, randomGenerator, group),
    HeterogeneousPedestrian<T, P, A>
    where P : Vector<P>, P : Position<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    private val speed = Speed(age, gender, randomGenerator)

    private val helpAttitude = HelpAttitude(age, gender)

    final override val compliance = Compliance(age, gender).level

    override val walkingSpeed = speed.walking

    override val runningSpeed = speed.running

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T, P, A>) =
        helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))
}
