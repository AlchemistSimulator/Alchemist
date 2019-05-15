package it.unibo.alchemist.characteristics.individual

import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian

class HelpAttitude(age: Age, gender: Gender) : IndividualCharacteristic {

    private val helperRules = rules[age to gender]

    fun level(toHelp: HeterogeneousPedestrian<*>): Double =
            helperRules?.get(toHelp.age to toHelp.gender)?.first ?: 0.0

    companion object {
        // first -> in the same group
        // second -> not in the same group
        private val rules: Map<Pair<Age, Gender>, Map<Pair<Age, Gender>, Pair<Double, Double>>> = mapOf(
            Pair(Age.ADULT, Gender.MALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to Pair(0.30, 0.25),
                Pair(Age.CHILD, Gender.FEMALE) to Pair(0.40, 0.34),
                Pair(Age.ADULT, Gender.MALE) to Pair(0.15, 0.13),
                Pair(Age.ADULT, Gender.FEMALE) to Pair(0.30, 0.25),
                Pair(Age.ELDERLY, Gender.MALE) to Pair(0.30, 0.25),
                Pair(Age.ELDERLY, Gender.FEMALE) to Pair(0.40, 0.34)
            ),

            Pair(Age.ADULT, Gender.FEMALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to Pair(0.15, 0.13),
                Pair(Age.CHILD, Gender.FEMALE) to Pair(0.20, 0.17),
                Pair(Age.ADULT, Gender.MALE) to Pair(0.08, 0.06),
                Pair(Age.ADULT, Gender.FEMALE) to Pair(0.15, 0.13),
                Pair(Age.ELDERLY, Gender.MALE) to Pair(0.15, 0.13),
                Pair(Age.ELDERLY, Gender.FEMALE) to Pair(0.20, 0.17)
            ),

            Pair(Age.ELDERLY, Gender.MALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to Pair(0.15, 0.13),
                Pair(Age.CHILD, Gender.FEMALE) to Pair(0.20, 0.17),
                Pair(Age.ADULT, Gender.MALE) to Pair(0.08, 0.06),
                Pair(Age.ADULT, Gender.FEMALE) to Pair(0.15, 0.13),
                Pair(Age.ELDERLY, Gender.MALE) to Pair(0.15, 0.13),
                Pair(Age.ELDERLY, Gender.FEMALE) to Pair(0.20, 0.17)
            ),

            Pair(Age.ELDERLY, Gender.FEMALE) to mapOf(
                Pair(Age.CHILD, Gender.MALE) to Pair(0.08, 0.06),
                Pair(Age.CHILD, Gender.FEMALE) to Pair(0.04, 0.03),
                Pair(Age.ADULT, Gender.MALE) to Pair(0.04, 0.03),
                Pair(Age.ADULT, Gender.FEMALE) to Pair(0.10, 0.08),
                Pair(Age.ELDERLY, Gender.MALE) to Pair(0.08, 0.06),
                Pair(Age.ELDERLY, Gender.FEMALE) to Pair(0.10, 0.08)
            )
        )
    }
}