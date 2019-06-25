package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Compliance
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Speed
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.HelpAttitude
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import org.apache.commons.math3.random.RandomGenerator

private val CHILD_KEYWORDS = setOf("child", "CHILD")
private val ADULT_KEYWORDS = setOf("adult", "ADULT")
private val ELDERLY_KEYWORDS = setOf("elderly", "ELDERLY")

private val MALE_KEYWORDS = setOf("male", "m", "MALE", "M")
private val FEMALE_KEYWORDS = setOf("female", "f", "FEMALE", "F")

abstract class AbstractHeterogeneousPedestrian<T>(
    env: Environment<T, *>,
    rg: RandomGenerator,
    ageString: String,
    genderString: String
) : AbstractHomogeneousPedestrian<T>(env), HeterogeneousPedestrian<T> {

    final override val age = when {
        CHILD_KEYWORDS.contains(ageString) -> Age.CHILD
        ADULT_KEYWORDS.contains(ageString) -> Age.ADULT
        ELDERLY_KEYWORDS.contains(ageString) -> Age.ELDERLY
        else -> throw IllegalArgumentException("$ageString is not a valid age")
    }

    final override val gender = when {
        MALE_KEYWORDS.contains(genderString) -> Gender.MALE
        FEMALE_KEYWORDS.contains(genderString) -> Gender.FEMALE
        else -> throw IllegalArgumentException("$genderString is not a valid gender")
    }

    private val speed = Speed(rg, age, gender)

    private val helpAttitude = HelpAttitude(age, gender)

    final override val compliance = Compliance(age, gender).level

    override val walkingSpeed = speed.walking

    override val runningSpeed = speed.running

    override fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>) =
        helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))
}