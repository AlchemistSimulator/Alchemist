package it.unibo.alchemist.model.cognitiveagents.impact.individual

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding compliance to load from a config file.
 */
object ComplianceSpec : ConfigSpec() {
    val childMale by required<Double>()
    val adultMale by required<Double>()
    val elderlyMale by required<Double>()
    val childFemale by required<Double>()
    val adultFemale by required<Double>()
    val elderlyFemale by required<Double>()
}
