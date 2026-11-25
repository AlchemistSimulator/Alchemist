package it.unibo.alchemist.model.cognitive.impact.individual

import it.unibo.alchemist.model.cognitive.impact.ImpactParameters

/**
 * The level of compliance of an agent considering its gender and its age.
 *
 * @param age
 *          the age of the agent.
 * @param gender
 *          the gender of the agent.
 */
class Compliance(age: Age, gender: Gender) : Characteristic {
    /**
     * The calculated level of compliance.
     */
    val level =
        when {
            age == Age.CHILD && gender == Gender.MALE -> childMale
            age == Age.ADULT && gender == Gender.MALE -> adultMale
            age == Age.ELDERLY && gender == Gender.MALE -> elderlyMale
            age == Age.CHILD && gender == Gender.FEMALE -> childFemale
            age == Age.ADULT && gender == Gender.FEMALE -> adultFemale
            else -> elderlyFemale
        }

    private companion object {
        private val parameters = ImpactParameters.default.compliance

        private val childMale = parameters.childMale
        private val adultMale = parameters.adultMale
        private val elderlyMale = parameters.elderlyMale
        private val childFemale = parameters.childFemale
        private val adultFemale = parameters.adultFemale
        private val elderlyFemale = parameters.elderlyFemale
    }
}
