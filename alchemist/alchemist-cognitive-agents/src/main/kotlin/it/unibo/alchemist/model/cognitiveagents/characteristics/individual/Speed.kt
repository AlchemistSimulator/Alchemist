package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

import com.uchuhimo.konf.Config
import it.unibo.alchemist.model.cognitiveagents.characteristics.PARAMETERS_FILE
import org.apache.commons.math3.random.RandomGenerator

class Speed(rg: RandomGenerator, age: Age, gender: Gender) : IndividualCharacteristic {

    private val individualFactor = { rg.nextDouble() * variance }

    val walking = when {
        age == Age.CHILD && gender == Gender.MALE -> childMale
        age == Age.CHILD && gender == Gender.FEMALE -> childFemale
        age == Age.ADULT && gender == Gender.MALE -> adultMale
        age == Age.ADULT && gender == Gender.FEMALE -> adultFemale
        age == Age.ELDERLY && gender == Gender.MALE -> elderlyMale
        else -> elderlyFemale
    } + individualFactor()

    val running = walking * 3

    companion object {
        private val config = Config { addSpec(SpeedSpec) }
                .from.toml.resource(PARAMETERS_FILE)

        val childMale = config[SpeedSpec.childMale]
        val adultMale = config[SpeedSpec.adultMale]
        val elderlyMale = config[SpeedSpec.elderlyMale]
        val childFemale = config[SpeedSpec.childFemale]
        val adultFemale = config[SpeedSpec.adultFemale]
        val elderlyFemale = config[SpeedSpec.elderlyFemale]
        val default = config[SpeedSpec.default]
        val variance = config[SpeedSpec.variance]
    }
}