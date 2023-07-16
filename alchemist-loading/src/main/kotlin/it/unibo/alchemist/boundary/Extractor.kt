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
 * {@link Environment}, given the current
 * {@link it.unibo.alchemist.core.Simulation} {@link Time}, the last
 * {@link Reaction} executed and the current simulation step.
 *
 */
interface Extractor<out E : Any> {

    /**
     * Extracts properties from an environment. The returned map must either:
     *   - contain a single element,
     *   - have the keys matching [columnNames], or
     *   - be iterable in predictable order
     *     (namely, implement [SortedMap] or extend one of [LinkedHashMap] or [ConcurrentLinkedHashMap]).
     *
     * @param environment
     *            the {@link Environment}
     * @param reaction
     *            the last executed {@link Reaction}
     * @param time
     *            the current {@link Time}
     * @param step
     *            the simulation step
     * @param <T> concentration type
     * @return
     *  the extracted properties with their names. The returned map must either:
     *      - contain a single element,
     *      - have the keys matching [columnNames], or
     *      - be iterable in predictable order
     *      (namely, implement [SortedMap] or extend [LinkedHashMap]).
     */
    fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, E>

    /**
     * Same as [extractData], but specifically meant for textual outputs. Captures [E] to [String] conversions.
     * The default implementation just runs a `toString()` conversion ver [extractData]'s return value's values.
     *
     * @param environment
     *            the {@link Environment}
     * @param reaction
     *            the last executed {@link Reaction}
     * @param time
     *            the current {@link Time}
     * @param step
     *            the simulation step
     * @param <T> concentration type
     * @return the extracted properties in textual format with their names.
     */
    fun <T> extractDataAsText(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, String> = extractData(environment, reaction, time, step).mapValues { it.value.toString() }

    /**
     * @return the name of the properties that this [Extractor] can
     * provide
     */
    val columnNames: List<String>
}
