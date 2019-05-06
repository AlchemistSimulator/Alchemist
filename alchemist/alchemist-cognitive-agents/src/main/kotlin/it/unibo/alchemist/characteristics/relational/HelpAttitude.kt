package it.unibo.alchemist.characteristics.relational

import it.unibo.alchemist.agents.Pedestrian
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Position

class HelpAttitude<T, P : Position<P>> : RelationalCharacteristic<T, P>() {

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

    fun level(toHelp: Pedestrian<T, P>): Double =
            rules[Pair(owner.age, owner.gender)]?.get(Pair(toHelp.age, toHelp.gender))
                    ?.let { it.second } ?: 0.0 // can't be part of the group yet
}