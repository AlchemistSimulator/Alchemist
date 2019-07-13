package it.unibo.alchemist.characteristics.individual

import org.apache.commons.math3.random.RandomGenerator

class Speed(rg: RandomGenerator, age: Age, gender: Gender) : IndividualCharacteristic {

    private val individualFactor = { rg.nextDouble() / 2 } // from 0.0 to 0.5

    val walking = when {
        age == Age.ELDERLY -> 0.9 + individualFactor()
        age == Age.ADULT && gender == Gender.MALE -> 1.0 + individualFactor()
        age == Age.ADULT && gender == Gender.FEMALE -> 0.9 + individualFactor()
        else -> 0.5 + individualFactor() // Age.CHILD
    }

    val running = walking * 3
}