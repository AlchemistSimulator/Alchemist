package it.unibo.alchemist.characteristics.implementations

import it.unibo.alchemist.characteristics.interfaces.IndividualCharacteristic
import kotlin.random.Random

class Speed(age: Age, gender: Gender) : IndividualCharacteristic {

    private val individualFactor = { Random.nextDouble(0.0, 0.5) }

    val walking = when {
        age == Age.ELDERLY -> 0.9 + individualFactor()
        age == Age.ADULT && gender == Gender.MALE -> 1.0 + individualFactor()
        age == Age.ADULT && gender == Gender.FEMALE -> 0.9 + individualFactor()
        else -> 0.5 + individualFactor() // Age.CHILD
    }

    val running = walking * 3

}