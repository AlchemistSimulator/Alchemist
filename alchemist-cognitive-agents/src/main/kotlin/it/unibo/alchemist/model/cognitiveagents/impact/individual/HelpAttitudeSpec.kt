package it.unibo.alchemist.model.cognitiveagents.impact.individual

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding help attitudes to load from a config file.
 */
object HelpAttitudeSpec : ConfigSpec() {

    /**
     * Adult males attitudes.
     */
    object AdultMale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    /**
     * Adult females attitudes.
     */
    object AdultFemale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    /**
     * Elderly males attitudes.
     */
    object ElderlyMale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    /**
     * Elderly females attitudes.
     */
    object ElderlyFemale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }
}
