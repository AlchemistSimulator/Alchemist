package it.unibo.alchemist.agents.heterogeneous

import it.unibo.alchemist.agents.homogeneous.AbstractHomogeneousPedestrian
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Compliance
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.characteristics.individual.Speed
import it.unibo.alchemist.characteristics.individual.HelpAttitude
import it.unibo.alchemist.model.interfaces.Environment
import org.apache.commons.math3.random.RandomGenerator

abstract class AbstractHeterogeneousPedestrian<T>(
    env: Environment<T, *>,
    rg: RandomGenerator,
    ageString: String,
    genderString: String
) : HeterogeneousPedestrian<T>, AbstractHomogeneousPedestrian<T>(env) {

    final override val age: Age = when (ageString) {
        "child", "CHILD" -> Age.CHILD
        "adult", "ADULT" -> Age.ADULT
        "elderly", "ELDERLY" -> Age.ELDERLY
        else -> throw IllegalArgumentException("$ageString is not a valid age")
    }

    final override val gender: Gender = when (genderString) {
        "male", "m", "MALE", "M" -> Gender.MALE
        "female", "f", "FEMALE", "F" -> Gender.FEMALE
        else -> throw IllegalArgumentException("$genderString is not a valid gender")
    }

    override val speed = Speed(rg, age, gender)

    override val compliance = Compliance(age, gender)

    private val helpAttitude = HelpAttitude(age, gender)

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>) =
            helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))
}