package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.HeterogeneousPedestrianModel
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.implementations.capabilities.BasicPedestrianMovementCapability
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianRunningCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianWalkingCapability
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a heterogeneous pedestrian.
 *
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 */
abstract class AbstractHeterogeneousPedestrian<T, P, A, F> @JvmOverloads constructor(
    randomGenerator: RandomGenerator,
    backingNode: Node<T>,
    age: Age,
    gender: Gender,
    group: PedestrianGroup<T, P, A>? = null
) : AbstractHomogeneousPedestrian<T, P, A, F>(randomGenerator, backingNode, group),
    HeterogeneousPedestrian<T, P, A>
    where P : Vector<P>, P : Position<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    final override val pedestrianModel: HeterogeneousPedestrianModel<T, P, A> = HeterogeneousPedestrianModel(
        age = age,
        gender = gender,
        speed = Speed(age, gender, randomGenerator),
    )

    init {
        addCapability(BasicPedestrianMovementCapability(pedestrianModel.speed.walking, pedestrianModel.speed.running))
    }

    override val walkingSpeed = asCapability(PedestrianWalkingCapability::class).walkingSpeed

    override val runningSpeed = asCapability(PedestrianRunningCapability::class).runningSpeed

//    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T, P, A>) =
//        model.helpAttitude.level(toHelp.model.age, toHelp.model.gender, membershipGroup.contains(toHelp))
}
