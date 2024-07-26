/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.observation.MutableObservable0.Companion.observing
import it.unibo.alchemist.model.observation.Observable

class TestObservables : StringSpec() {

    class Node {
        private val internalContents: MutableMap<String, String> = mutableMapOf()
        private var observableContents = observing(internalContents)

        val contents: Observable<Map<String, String>> = observableContents

        operator fun set(key: String, value: String) {
            internalContents[key] = value
            observableContents.markChanged()
        }

        operator fun get(key: String): Observable<String> = observableContents.map { it[key].orEmpty() }
    }

    class TimeDist

    init {
        "Observable" {
            val observable = observing(1)
            observable.onChange(this) { println(it) }
            observable.onChange(this) { println(it) }
            observable.onChange(this) { println(it) }
            observable.onChange(this) { println(it) }
        }
    }
}
