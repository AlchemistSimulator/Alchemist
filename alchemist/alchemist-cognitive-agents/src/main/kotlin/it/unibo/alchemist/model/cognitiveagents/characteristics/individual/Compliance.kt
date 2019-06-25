package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

import com.uchuhimo.konf.Config
import it.unibo.alchemist.model.cognitiveagents.characteristics.PARAMETERS_FILE

class Compliance(age: Age, gender: Gender) : IndividualCharacteristic {

    val level = when {
        age == Age.ELDERLY && gender == Gender.MALE -> 0.92
        age == Age.ELDERLY && gender == Gender.FEMALE -> 0.97
        age == Age.ADULT && gender == Gender.MALE -> 0.89
        age == Age.ADULT && gender == Gender.FEMALE -> 0.94
        else -> 0.89 // Age.CHILD
    }

    companion object {
        private val config = Config { addSpec(ComplianceSpec) }
                .from.toml.resource(PARAMETERS_FILE)

        val childMale = config[ComplianceSpec.childMale]
        val adultMale = config[ComplianceSpec.adultMale]
        val elderlyMale = config[ComplianceSpec.elderlyMale]
        val childFemale = config[ComplianceSpec.childFemale]
        val adultFemale = config[ComplianceSpec.adultFemale]
        val elderlyFemale = config[ComplianceSpec.elderlyFemale]
    }
}