package it.unibo.alchemist.model.cognitive.impact.individual

import it.unibo.alchemist.model.cognitive.impact.ImpactParameters

/**
 * The attitude of an agent towards helping another agent.
 *
 * @param age
 *          the age of the helper.
 * @param gender
 *          the gender of the helper.
 */
class HelpAttitude(age: Age, gender: Gender) : Characteristic {
    private val helperRules = rules[age to gender]

    /**
     * The logic used to calculate the probability of helping.
     *
     * @param toHelpAge
     *          the age of the agent who needs help.
     * @param toHelpGender
     *          the gender of the agent who needs help.
     * @param sameGroup
     *          whether or not the two agents belong to the same group.
     */
    fun level(toHelpAge: Age, toHelpGender: Gender, sameGroup: Boolean): Double =
        helperRules?.get(toHelpAge to toHelpGender)?.let {
            if (sameGroup) it.first else it.second
        } ?: 0.0

    private companion object {
        private val parameters = ImpactParameters.default.helpAttitude

        private val rules: Map<Pair<Age, Gender>, Map<Pair<Age, Gender>, Pair<Double, Double>>> =
            mapOf(
                Pair(Age.ADULT, Gender.MALE) to
                    mapOf(
                        Pair(Age.CHILD, Gender.MALE) to parameters.adultMale.childMale,
                        Pair(Age.ADULT, Gender.MALE) to parameters.adultMale.adultMale,
                        Pair(Age.ELDERLY, Gender.MALE) to parameters.adultMale.elderlyMale,
                        Pair(Age.CHILD, Gender.FEMALE) to parameters.adultMale.childFemale,
                        Pair(Age.ADULT, Gender.FEMALE) to parameters.adultMale.adultFemale,
                        Pair(Age.ELDERLY, Gender.FEMALE) to parameters.adultMale.elderlyFemale,
                    ),
                Pair(Age.ADULT, Gender.FEMALE) to
                    mapOf(
                        Pair(Age.CHILD, Gender.MALE) to parameters.adultFemale.childMale,
                        Pair(Age.ADULT, Gender.MALE) to parameters.adultFemale.adultMale,
                        Pair(Age.ELDERLY, Gender.MALE) to parameters.adultFemale.elderlyMale,
                        Pair(Age.CHILD, Gender.FEMALE) to parameters.adultFemale.childFemale,
                        Pair(Age.ADULT, Gender.FEMALE) to parameters.adultFemale.adultFemale,
                        Pair(Age.ELDERLY, Gender.FEMALE) to parameters.adultFemale.elderlyFemale,
                    ),
                Pair(Age.ELDERLY, Gender.MALE) to
                    mapOf(
                        Pair(Age.CHILD, Gender.MALE) to parameters.elderlyMale.childMale,
                        Pair(Age.ADULT, Gender.MALE) to parameters.elderlyMale.adultMale,
                        Pair(Age.ELDERLY, Gender.MALE) to parameters.elderlyMale.elderlyMale,
                        Pair(Age.CHILD, Gender.FEMALE) to parameters.elderlyMale.childFemale,
                        Pair(Age.ADULT, Gender.FEMALE) to parameters.elderlyMale.adultFemale,
                        Pair(Age.ELDERLY, Gender.FEMALE) to parameters.elderlyMale.elderlyFemale,
                    ),
                Pair(Age.ELDERLY, Gender.FEMALE) to
                    mapOf(
                        Pair(Age.CHILD, Gender.MALE) to parameters.elderlyFemale.childMale,
                        Pair(Age.ADULT, Gender.MALE) to parameters.elderlyFemale.adultMale,
                        Pair(Age.ELDERLY, Gender.MALE) to parameters.elderlyFemale.elderlyMale,
                        Pair(Age.CHILD, Gender.FEMALE) to parameters.elderlyFemale.childFemale,
                        Pair(Age.ADULT, Gender.FEMALE) to parameters.elderlyFemale.adultFemale,
                        Pair(Age.ELDERLY, Gender.FEMALE) to parameters.elderlyFemale.elderlyFemale,
                    ),
            )
    }
}
