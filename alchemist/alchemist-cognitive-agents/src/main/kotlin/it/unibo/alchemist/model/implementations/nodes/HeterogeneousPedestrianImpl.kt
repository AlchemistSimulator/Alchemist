package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.*
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of an heterogeneous pedestrian.
 */
open class HeterogeneousPedestrianImpl<T>(
    env: Environment<T, *>,
    rg: RandomGenerator,
    final override val age: Age,
    final override val gender: Gender
) : HomogeneousPedestrianImpl<T>(env), HeterogeneousPedestrian<T> {

    private val speed = Speed(age, gender, rg)

    private val helpAttitude = HelpAttitude(age, gender)

    final override val compliance = Compliance(age, gender).level

    override val walkingSpeed = speed.walking

    override val runningSpeed = speed.running

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>) =
            helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))
}