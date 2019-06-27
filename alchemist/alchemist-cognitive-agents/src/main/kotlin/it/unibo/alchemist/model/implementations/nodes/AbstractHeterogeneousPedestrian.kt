package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Compliance
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.HelpAttitude
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import org.apache.commons.math3.random.RandomGenerator

abstract class AbstractHeterogeneousPedestrian<T>(
    env: Environment<T, *>,
    rg: RandomGenerator,
    final override val age: Age,
    final override val gender: Gender
) : AbstractHomogeneousPedestrian<T>(env), HeterogeneousPedestrian<T> {

    private val speed = Speed(rg, age, gender)

    private val helpAttitude = HelpAttitude(age, gender)

    final override val compliance = Compliance(age, gender).level

    override val walkingSpeed = speed.walking

    override val runningSpeed = speed.running

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>) =
        helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))
}