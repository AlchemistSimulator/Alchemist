package it.unibo.alchemist.model.cognitive.impact.individual

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import it.unibo.alchemist.model.cognitive.impact.PARAMETERS_FILE
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import org.apache.commons.math3.random.RandomGenerator

/**
 * The speed of an agent considering its age, gender and a random factor.
 *
 * @param age
 *          the age of the agent.
 * @param gender
 *          the gender of the agent.
 * @param randomGenerator
 *          the simulation {@link RandomGenerator}.
 */
class Speed(age: Age, gender: Gender, randomGenerator: RandomGenerator) : Characteristic {

    /**
     * The walking speed of the agent.
     */
    val walking = when {
        age == Age.CHILD && gender == Gender.MALE -> childMale
        age == Age.CHILD && gender == Gender.FEMALE -> childFemale
        age == Age.ADULT && gender == Gender.MALE -> adultMale
        age == Age.ADULT && gender == Gender.FEMALE -> adultFemale
        age == Age.ELDERLY && gender == Gender.MALE -> elderlyMale
        else -> elderlyFemale
    } + randomGenerator.nextDouble(0.0, variance)

    /**
     * The running speed of the agent.
     */
    val running = walking * 3

    companion object {
        private val config = Config { addSpec(SpeedSpec) }.from.toml.resource(PARAMETERS_FILE)
        private val childMale: Double = config[SpeedSpec.childMale]
        private val adultMale: Double = config[SpeedSpec.adultMale]
        private val elderlyMale: Double = config[SpeedSpec.elderlyMale]
        private val childFemale: Double = config[SpeedSpec.childFemale]
        private val adultFemale: Double = config[SpeedSpec.adultFemale]
        private val elderlyFemale: Double = config[SpeedSpec.elderlyFemale]

        /**
         * Default speed.
         */
        val default: Double = config[SpeedSpec.default]
        private val variance: Double = config[SpeedSpec.variance]
    }
}
