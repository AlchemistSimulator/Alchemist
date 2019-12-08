/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.variables

import com.google.common.collect.Streams
import java.util.stream.Stream

/**
 * A list of arbitrary values.
 */
class ListOf<T : java.io.Serializable>(
    private val default: T,
    private vararg val values: T
) : PrintableVariable<T>() {
    override fun getDefault() = default

    override fun stream(): Stream<T> = Streams.concat(Stream.of(default), Stream.of(*values))
}
