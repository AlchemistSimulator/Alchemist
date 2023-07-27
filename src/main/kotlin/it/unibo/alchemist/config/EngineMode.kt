/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.config

/**
 * Engine modes.
 * @property code mode code
 */
enum class EngineMode(val code: String) {

    /**
     * Launch simulation is single-threaded deterministic mode.
     */
    DETERMINISTIC("deterministic"),

    /**
     *  Launch simulation in fixed batch size mode.
     */
    BATCH_FIXED("batchFixed"),

    /**
     *  Launch simulation in epsilon batch mode.
     */
    BATCH_EPSILON("batchEpsilon"), ;

    companion object {
        /**
         * Parse string into EngineMode.
         */
        fun parseCode(code: String): EngineMode {
            val match = EngineMode.values().find { it.code == code }
            return requireNotNull(match) {
                "Unknown EngineMode value $code, allowed: [${EngineMode.values()}]"
            }
        }
    }
}
