/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import java.io.Serializable
import java.util.stream.Stream
import javax.annotation.Nonnull

/**
 * A variable simulation value that provides a range of values for batches, and
 * a default value for single-shot runs.
 *
 * @param <V> value typ of the variable
</V> */
interface Variable<V : Serializable?> :
    Serializable,
    Iterable<V> {

    @Nonnull
    override fun iterator(): MutableIterator<V> = stream().iterator()

    /**
     * @return the number of different values this [Variable] may yield
     */
    fun steps(): Long = stream().count()

    /**
     * @return the default value for this [Variable]
     */
    val default: V

    /**
     * @return all values of this variable as [java.util.stream.Stream].
     */
    fun stream(): Stream<V>
}
