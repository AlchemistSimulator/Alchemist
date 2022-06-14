/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Actionable
import it.unibo.alchemist.model.interfaces.Time

/**
 * An object that is able to extract numeric informations from an Alchemist
 * {@link Environment}, given the current
 * {@link it.unibo.alchemist.core.interfaces.Simulation} {@link Time}, the last
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
     * @return the name of the properties that this [Extractor] can
     * provide
     */
    val columnNames: List<String>
}
