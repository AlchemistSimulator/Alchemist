package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

import com.uchuhimo.konf.Config
import it.unibo.alchemist.model.cognitiveagents.characteristics.Characteristic
import it.unibo.alchemist.model.cognitiveagents.characteristics.PARAMETERS_FILE

/**
 * The level of compliance of an agent considering its gender and its age.
 *
 * @param age
 *          the age of the agent.
 * @param gender
 *          the gender of the agent.
 */
class Compliance(age: Age, gender: Gender) : Characteristic {

    /**
     * The calculated level of compliance.
     */
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