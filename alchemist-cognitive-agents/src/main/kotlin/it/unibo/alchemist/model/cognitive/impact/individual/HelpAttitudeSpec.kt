/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact.individual

import com.uchuhimo.konf.ConfigSpec

private typealias DoublePair = Pair<Double, Double>

/**
 * A specification of the parameters regarding help attitudes to load from a config file.
 */
object HelpAttitudeSpec : ConfigSpec() {

    // CPD-OFF

    /**
     * Adult males' help attitudes.
     */
    object AdultMale : ConfigSpec() {

        /**
         * The attitude of an adult male to help a male child.
         */
        val childMale by required<DoublePair>()

        /**
         * The attitude of an adult male to help a male adult.
         */
        val adultMale by required<DoublePair>()

        /**
         * The attitude of an adult male to help a male elderly.
         */
        val elderlyMale by required<DoublePair>()

        /**
         * The attitude of an adult male to help a female child.
         */
        val childFemale by required<DoublePair>()

        /**
         * The attitude of an adult male to help an adult female.
         */
        val adultFemale by required<DoublePair>()

        /**
         * The attitude of an adult male to help an elderly woman.
         */
        val elderlyFemale by required<DoublePair>()
    }

    /**
     * Adult females attitudes.
     */
    object AdultFemale : ConfigSpec() {

        /**
         * The attitude of an adult female to help a male child.
         */
        val childMale by required<DoublePair>()

        /**
         * The attitude of an adult female to help a male adult.
         */
        val adultMale by required<DoublePair>()

        /**
         * The attitude of an adult female to help a male elderly.
         */
        val elderlyMale by required<DoublePair>()

        /**
         * The attitude of an adult female to help a female child.
         */
        val childFemale by required<DoublePair>()

        /**
         * The attitude of an adult female to help an adult female.
         */
        val adultFemale by required<DoublePair>()

        /**
         * The attitude of an adult female to help an elderly woman.
         */
        val elderlyFemale by required<DoublePair>()
    }

    /**
     * Elderly males attitudes.
     */
    object ElderlyMale : ConfigSpec() {

        /**
         * The attitude of an elderly male to help a male child.
         */
        val childMale by required<DoublePair>()

        /**
         * The attitude of an elderly male to help a male adult.
         */
        val adultMale by required<DoublePair>()

        /**
         * The attitude of an elderly male to help a male elderly.
         */
        val elderlyMale by required<DoublePair>()

        /**
         * The attitude of an elderly male to help a female child.
         */
        val childFemale by required<DoublePair>()

        /**
         * The attitude of an elderly male to help an adult female.
         */
        val adultFemale by required<DoublePair>()

        /**
         * The attitude of an elderly male to help an elderly woman.
         */
        val elderlyFemale by required<DoublePair>()
    }

    /**
     * Elderly females attitudes.
     */
    object ElderlyFemale : ConfigSpec() {

        /**
         * The attitude of an elderly female to help a male child.
         */
        val childMale by required<DoublePair>()

        /**
         * The attitude of an elderly female to help a male adult.
         */
        val adultMale by required<DoublePair>()

        /**
         * The attitude of an elderly female to help a male elderly.
         */
        val elderlyMale by required<DoublePair>()

        /**
         * The attitude of an elderly female to help a female child.
         */
        val childFemale by required<DoublePair>()

        /**
         * The attitude of an elderly female to help an adult female.
         */
        val adultFemale by required<DoublePair>()

        /**
         * The attitude of an elderly female to help an elderly woman.
         */
        val elderlyFemale by required<DoublePair>()
    }

    // CPD-ON
}
