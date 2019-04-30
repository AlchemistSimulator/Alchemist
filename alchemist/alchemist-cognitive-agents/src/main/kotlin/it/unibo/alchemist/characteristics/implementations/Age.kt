package it.unibo.alchemist.characteristics.implementations

import it.unibo.alchemist.characteristics.interfaces.IndividualCharacteristic

const val CHILD_THRESHOLD = 18

const val ADULT_THRESHOLD = 60

enum class Age(min: Int, max: Int) : IndividualCharacteristic {

    CHILD(0, CHILD_THRESHOLD - 1), ADULT(CHILD_THRESHOLD, ADULT_THRESHOLD - 1), ELDERLY(ADULT_THRESHOLD, Int.MAX_VALUE);

    fun getCategory(age: Int): Age = when {
        age < CHILD_THRESHOLD -> CHILD
        age < ADULT_THRESHOLD -> ADULT
        else -> ELDERLY
    }

}