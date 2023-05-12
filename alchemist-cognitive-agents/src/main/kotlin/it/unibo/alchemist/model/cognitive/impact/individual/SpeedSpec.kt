package it.unibo.alchemist.model.cognitive.impact.individual

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding speeds to load from a config file.
 */
object SpeedSpec : ConfigSpec() {

    /**
     * The walking speed of a young male.
     */
    val childMale by required<Double>()

    /**
     * The walking speed of an adult male.
     */
    val adultMale by required<Double>()

    /**
     * The walking speed of an elderly male.
     */
    val elderlyMale by required<Double>()

    /**
     * The walking speed of a young female.
     */
    val childFemale by required<Double>()

    /**
     * The walking speed of an adult female.
     */
    val adultFemale by required<Double>()

    /**
     * The walking speed of an elderly female.
     */
    val elderlyFemale by required<Double>()

    /**
     * The default walking speed, if the pedestrian details have not been defined.
     */
    val default by required<Double>()

    /**
     * The walking speed variance.
     */
    val variance by required<Double>()
}
