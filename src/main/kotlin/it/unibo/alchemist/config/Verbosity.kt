/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.config

import ch.qos.logback.classic.Level

/**
 * Log verbosity configuration.
 *
 * @property code verbosity code
 * @property logLevel logging level mapping
 */
enum class Verbosity(val code: String, val logLevel: Level) {

    /**
     * Debug.
     */
    DEBUG("debug", Level.DEBUG),

    /**
     * INFO.
     */
    INFO("info", Level.INFO),

    /**
     * WARN.
     */
    WARN("warn", Level.WARN),

    /**
     * ERROR.
     */
    ERROR("error", Level.ERROR),

    /**
     * ALL.
     */
    ALL("all", Level.ALL),

    /**
     * OFF.
     */
    OFF("off", Level.OFF),
}
