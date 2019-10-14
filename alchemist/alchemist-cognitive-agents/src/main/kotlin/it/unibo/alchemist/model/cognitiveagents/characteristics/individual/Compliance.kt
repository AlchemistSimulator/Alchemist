package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

import com.uchuhimo.konf.Config
import it.unibo.alchemist.model.cognitiveagents.characteristics.PARAMETERS_FILE

class Compliance(age: Age, gender: Gender) : IndividualCharacteristic {

    val level = when {
        age == Age.CHILD && gender == Gender.MALE -> childMale
        age == Age.ADULT && gender == Gender.MALE -> adultMale
        age == Age.ELDERLY && gender == Gender.MALE -> elderlyMale
        age == Age.CHILD && gender == Gender.FEMALE -> childFemale
        age == Age.ADULT && gender == Gender.FEMALE -> adultFemale
        else -> elderlyFemale
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