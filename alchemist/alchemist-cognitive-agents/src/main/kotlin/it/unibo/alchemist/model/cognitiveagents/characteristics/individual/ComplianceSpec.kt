package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

import com.uchuhimo.konf.ConfigSpec

object ComplianceSpec : ConfigSpec() {
    val childMale by required<Double>()
    val adultMale by required<Double>()
    val elderlyMale by required<Double>()
    val childFemale by required<Double>()
    val adultFemale by required<Double>()
    val elderlyFemale by required<Double>()
}