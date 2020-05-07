package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Compliance
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.HelpAttitude
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of an heterogeneous pedestrian.
 *
 * @param env
 *          the environment inside which this pedestrian moves.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param age
 *          the age of this pedestrian.
 * @param gender
 *          the gender of this pedestrian
 */
open class HeterogeneousPedestrianImpl<T, P : Position<P>> @JvmOverloads constructor(
    env: Environment<T, P>,
    rg: RandomGenerator,
    final override val age: Age,
    final override val gender: Gender,
    group: PedestrianGroup<T>? = null
) : HomogeneousPedestrianImpl<T, P>(env, rg, group), HeterogeneousPedestrian<T> {

    private val speed = Speed(age, gender, rg)

    private val helpAttitude = HelpAttitude(age, gender)

    final override val compliance = Compliance(age, gender).level

    override val walkingSpeed = speed.walking

    override val runningSpeed = speed.running

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>) =
        helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))
}
