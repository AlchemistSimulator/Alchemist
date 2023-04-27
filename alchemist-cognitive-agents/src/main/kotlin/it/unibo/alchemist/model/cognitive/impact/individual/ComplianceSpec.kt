package it.unibo.alchemist.model.cognitive.impact.individual

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding compliance to load from a config file.
 */
object ComplianceSpec : ConfigSpec() {

    /**
     * Compliance of a male child.
     */
    val childMale by required<Double>()

    /**
     * Compliance of a male adult.
     */
    val adultMale by required<Double>()

    /**
     * Compliance of a male elderly.
     */
    val elderlyMale by required<Double>()

    /**
     * Compliance of a female child.
     */
    val childFemale by required<Double>()

    /**
     * Compliance of a female adult.
     */
    val adultFemale by required<Double>()

    /**
     * Compliance of a female elderly.
     */
    val elderlyFemale by required<Double>()
}
