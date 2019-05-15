package it.unibo.alchemist.characteristics.cognitive

import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender

class HelpAttitude(
    private val age: Age,
    private val gender: Gender,
    private val toHelp: () -> Pair<Age, Gender>
) : CognitiveCharacteristic {

    private val helperRules = rules[age to gender]!!

    override fun level(): Double =
            helperRules[toHelp()]?.first ?: 0.0

    companion object {
        // first -> in the same group level
        // second -> not in the same group level
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