/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time

/**
 * An object that is able to extract numeric informations from an Alchemist
 * [Environment], given the current simulation [Time], the last executed
 * [Actionable] (usually a [Reaction] or null) and the current simulation step.
 */
interface Extractor<out E : Any> {
    /**
     * Extracts properties from an environment.
     *
     * Implementations must return a map whose iteration order is predictable or whose
     * keys match [columnNames]. Concretely the returned map must satisfy at least one
     * of the following:
     *  - contain a single element,
     *  - have keys matching [columnNames], or
     *  - be an ordered map (for example a [java.util.SortedMap], a [java.util.LinkedHashMap]
     *    or any map with deterministic iteration order).
     *
     * @param environment the environment from which to extract values
     * @param reaction the last executed actionable (may be null)
     * @param time the current simulation time
     * @param step the current simulation step
     * @param T the concentration type used by the environment
     * @return a map of property names to extracted values. The map must follow one of the
     *         ordering/key contracts described above so that callers can consistently
     *         determine column ordering when producing tabular outputs.
     */
    fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, E>

    /**
     * Same as [extractData], but specifically meant for textual outputs.
     *
     * The default implementation converts each extracted value to its textual
     * representation via [Any.toString]. Implementations may override this method
     * to provide custom formatting (for example numeric formatting or localized
     * representations).
     *
     * @param environment the environment from which to extract values
     * @param reaction the last executed actionable (may be null)
     * @param time the current simulation time
     * @param step the current simulation step
     * @param T the concentration type used by the environment
     * @return a map of property names to their textual representations
     */
    fun <T> extractDataAsText(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, String> = extractData(environment, reaction, time, step).mapValues { it.value.toString() }

    /**
     * The names of the properties that this [Extractor] can provide as columns.
     *
     * Implementations should return a list whose order matches the expected
     * column ordering when the returned map from [extractData] uses the same keys.
     */
    val columnNames: List<String>
}
