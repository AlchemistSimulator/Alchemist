package it.unibo.alchemist.model.cognitiveagents.impact.individual

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding help attitudes to load from a config file.
 */
object HelpAttitudeSpec : ConfigSpec() {

    /**
     * Adult males' help attitudes.
     */
    object AdultMale : ConfigSpec() {

        /**
         * The attitude of an adult male to help a male child.
         */
        val childMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult male to help a male adult.
         */
        val adultMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult male to help a male elderly.
         */
        val elderlyMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult male to help a female child.
         */
        val childFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult male to help an adult female.
         */
        val adultFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult male to help an elderly woman.
         */
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    /**
     * Adult females attitudes.
     */
    object AdultFemale : ConfigSpec() {

        /**
         * The attitude of an adult female to help a male child.
         */
        val childMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult female to help a male adult.
         */
        val adultMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult female to help a male elderly.
         */
        val elderlyMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult female to help a female child.
         */
        val childFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult female to help an adult female.
         */
        val adultFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an adult female to help an elderly woman.
         */
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    /**
     * Elderly males attitudes.
     */
    object ElderlyMale : ConfigSpec() {

        /**
         * The attitude of an elderly male to help a male child.
         */
        val childMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly male to help a male adult.
         */
        val adultMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly male to help a male elderly.
         */
        val elderlyMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly male to help a female child.
         */
        val childFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly male to help an adult female.
         */
        val adultFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly male to help an elderly woman.
         */
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    /**
     * Elderly females attitudes.
     */
    object ElderlyFemale : ConfigSpec() {

        /**
         * The attitude of an elderly female to help a male child.
         */
        val childMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly female to help a male adult.
         */
        val adultMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly female to help a male elderly.
         */
        val elderlyMale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly female to help a female child.
         */
        val childFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly female to help an adult female.
         */
        val adultFemale by required<Pair<Double, Double>>()

        /**
         * The attitude of an elderly female to help an elderly woman.
         */
        val elderlyFemale by required<Pair<Double, Double>>()
    }
}
