package it.unibo.alchemist.model.cognitiveagents.impact.individual

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import it.unibo.alchemist.model.cognitiveagents.impact.PARAMETERS_FILE

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

        private val childMale = config[ComplianceSpec.childMale]
        private val adultMale = config[ComplianceSpec.adultMale]
        private val elderlyMale = config[ComplianceSpec.elderlyMale]
        private val childFemale = config[ComplianceSpec.childFemale]
        private val adultFemale = config[ComplianceSpec.adultFemale]
        private val elderlyFemale = config[ComplianceSpec.elderlyFemale]
    }
}
