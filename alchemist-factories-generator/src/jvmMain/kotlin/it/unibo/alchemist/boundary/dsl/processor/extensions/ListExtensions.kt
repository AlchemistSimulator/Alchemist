/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.extensions

internal fun <T> List<T>.powerSetWithoutEmptySet(): Sequence<List<T>> = sequence {
    val numberOfSubsets = 1 shl size // 2^n subsets
    for (i in 1 until numberOfSubsets) {
        val subset = mutableListOf<T>()
        for (j in 0 until size) {
            if (i and (1 shl j) != 0) {
                subset.add(this@powerSetWithoutEmptySet[j])
            }
        }
        yield(subset)
    }
}
