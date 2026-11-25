package it.unibo.alchemist.model.cognitive.impact.individual

import it.unibo.alchemist.model.cognitive.impact.ImpactParameters
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
    val walking =
        when {
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

    /**
     * Default configurations.
     */
    companion object {
        private val parameters = ImpactParameters.default.speed
        private val childMale: Double = parameters.childMale
        private val adultMale: Double = parameters.adultMale
        private val elderlyMale: Double = parameters.elderlyMale
        private val childFemale: Double = parameters.childFemale
        private val adultFemale: Double = parameters.adultFemale
        private val elderlyFemale: Double = parameters.elderlyFemale

        /**
         * Default speed.
         */
        val default: Double = parameters.default
        private val variance: Double = parameters.variance
    }
}
