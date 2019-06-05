package it.unibo.alchemist.characteristics.individual

class Compliance(age: Age, gender: Gender) : IndividualCharacteristic {

    val level = when {
        age == Age.ELDERLY && gender == Gender.MALE -> 0.92
        age == Age.ELDERLY && gender == Gender.FEMALE -> 0.97
        age == Age.ADULT && gender == Gender.MALE -> 0.89
        age == Age.ADULT && gender == Gender.FEMALE -> 0.94
        else -> 0.89 // Age.CHILD
    }
}