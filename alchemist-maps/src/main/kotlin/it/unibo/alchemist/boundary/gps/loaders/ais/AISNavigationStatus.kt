/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

// AIS navigational statuses are an external standards table: inline numeric codes are intentional.
@file:Suppress("MagicNumber", "UndocumentedPublicClass", "UndocumentedPublicProperty")

package it.unibo.alchemist.boundary.gps.loaders.ais

/**
 * AIS navigational status codes.
 *
 * @property code raw AIS navigational status code.
 * @property description human-readable AIS navigational status description.
 */
sealed interface AISNavigationStatus {
    val code: Int
    val description: String

    data object UnderWayUsingEngine : AISNavigationStatus {
        override val code = 0
        override val description = "Under way using engine"
    }

    data object AtAnchor : AISNavigationStatus {
        override val code = 1
        override val description = "At anchor"
    }

    data object NotUnderCommand : AISNavigationStatus {
        override val code = 2
        override val description = "Not under command"
    }

    data object RestrictedManoeuverability : AISNavigationStatus {
        override val code = 3
        override val description = "Restricted manoeuverability"
    }

    data object ConstrainedByDraught : AISNavigationStatus {
        override val code = 4
        override val description = "Constrained by her draught"
    }

    data object Moored : AISNavigationStatus {
        override val code = 5
        override val description = "Moored"
    }

    data object Aground : AISNavigationStatus {
        override val code = 6
        override val description = "Aground"
    }

    data object EngagedInFishing : AISNavigationStatus {
        override val code = 7
        override val description = "Engaged in Fishing"
    }

    data object UnderWaySailing : AISNavigationStatus {
        override val code = 8
        override val description = "Under way sailing"
    }

    data object ReservedForHighSpeedCraft : AISNavigationStatus {
        override val code = 9
        override val description = "Reserved for future amendment of Navigational Status for HSC"
    }

    data object ReservedForWingInGround : AISNavigationStatus {
        override val code = 10
        override val description = "Reserved for future amendment of Navigational Status for WIG"
    }

    data class ReservedForFutureUse(override val code: Int) : AISNavigationStatus {
        init {
            require(code in 11..13) { "Reserved AIS navigational status code must be in 11..13" }
        }

        override val description = "Reserved for future use"
    }

    data object AisSartActive : AISNavigationStatus {
        override val code = 14
        override val description = "AIS-SART is active"
    }

    data object NotDefined : AISNavigationStatus {
        override val code = 15
        override val description = "Not defined (default)"
    }

    /**
     * Lookup helpers for AIS navigational status codes.
     */
    companion object {
        /**
         * Returns the AIS navigational status associated with [code], or `null` for values outside the AIS table.
         */
        fun fromCode(code: Int): AISNavigationStatus? = when (code) {
            0 -> UnderWayUsingEngine
            1 -> AtAnchor
            2 -> NotUnderCommand
            3 -> RestrictedManoeuverability
            4 -> ConstrainedByDraught
            5 -> Moored
            6 -> Aground
            7 -> EngagedInFishing
            8 -> UnderWaySailing
            9 -> ReservedForHighSpeedCraft
            10 -> ReservedForWingInGround
            in 11..13 -> ReservedForFutureUse(code)
            14 -> AisSartActive
            15 -> NotDefined
            else -> null
        }
    }
}
