package it.unibo.alchemist.model.cognitiveagents.impact.individual

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import it.unibo.alchemist.model.cognitiveagents.impact.PARAMETERS_FILE

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

    companion object {

        private val config = Config {
            addSpec(HelpAttitudeSpec)
        }.from.toml.resource(PARAMETERS_FILE)

        private val rules: Map<Pair<Age, Gender>, Map<Pair<Age, Gender>, Pair<Double, Double>>> = mapOf(
            Pair(Age.ADULT, Gender.MALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to config[HelpAttitudeSpec.AdultMale.childMale],
                Pair(Age.ADULT, Gender.MALE) to config[HelpAttitudeSpec.AdultMale.adultMale],
                Pair(Age.ELDERLY, Gender.MALE) to config[HelpAttitudeSpec.AdultMale.elderlyMale],
                Pair(Age.CHILD, Gender.FEMALE) to config[HelpAttitudeSpec.AdultMale.childFemale],
                Pair(Age.ADULT, Gender.FEMALE) to config[HelpAttitudeSpec.AdultMale.adultFemale],
                Pair(Age.ELDERLY, Gender.FEMALE) to config[HelpAttitudeSpec.AdultMale.elderlyFemale]
            ),

            Pair(Age.ADULT, Gender.FEMALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to config[HelpAttitudeSpec.AdultFemale.childMale],
                Pair(Age.ADULT, Gender.MALE) to config[HelpAttitudeSpec.AdultFemale.adultMale],
                Pair(Age.ELDERLY, Gender.MALE) to config[HelpAttitudeSpec.AdultFemale.elderlyMale],
                Pair(Age.CHILD, Gender.FEMALE) to config[HelpAttitudeSpec.AdultFemale.childFemale],
                Pair(Age.ADULT, Gender.FEMALE) to config[HelpAttitudeSpec.AdultFemale.adultFemale],
                Pair(Age.ELDERLY, Gender.FEMALE) to config[HelpAttitudeSpec.AdultFemale.elderlyFemale]
            ),

            Pair(Age.ELDERLY, Gender.MALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to config[HelpAttitudeSpec.ElderlyMale.childMale],
                Pair(Age.ADULT, Gender.MALE) to config[HelpAttitudeSpec.ElderlyMale.adultMale],
                Pair(Age.ELDERLY, Gender.MALE) to config[HelpAttitudeSpec.ElderlyMale.elderlyMale],
                Pair(Age.CHILD, Gender.FEMALE) to config[HelpAttitudeSpec.ElderlyMale.childFemale],
                Pair(Age.ADULT, Gender.FEMALE) to config[HelpAttitudeSpec.ElderlyMale.adultFemale],
                Pair(Age.ELDERLY, Gender.FEMALE) to config[HelpAttitudeSpec.ElderlyMale.elderlyFemale]
            ),

            Pair(Age.ELDERLY, Gender.FEMALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to config[HelpAttitudeSpec.ElderlyFemale.childMale],
                Pair(Age.ADULT, Gender.MALE) to config[HelpAttitudeSpec.ElderlyFemale.adultMale],
                Pair(Age.ELDERLY, Gender.MALE) to config[HelpAttitudeSpec.ElderlyFemale.elderlyMale],
                Pair(Age.CHILD, Gender.FEMALE) to config[HelpAttitudeSpec.ElderlyFemale.childFemale],
                Pair(Age.ADULT, Gender.FEMALE) to config[HelpAttitudeSpec.ElderlyFemale.adultFemale],
                Pair(Age.ELDERLY, Gender.FEMALE) to config[HelpAttitudeSpec.ElderlyFemale.elderlyFemale]
            )
        )
    }
}
