/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

/**
 *  Experimental feature flags used in CLI.
 *  @property code feature flag variable name
 */
enum class ExperimentalFeatureFlag(val code: String) {

    /**
     * Specify engine mode.
     */
    ENGINE_MODE("mode"),

    /**
     * Used only in batch fixed-size mode, determines batch size (defaults to available cores).
     */
    FIXED_BATCH_ENGINE_SIZE("batchSize"),

    /**
     * Used only in batch epsilon mode, determines epsilon value (defaults to 0.01).
     */
    EPSILON_BATCH_ENGINE_VALUE("epsilonSize"),
}
