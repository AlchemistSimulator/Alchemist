/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.some
import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.ObservableLists.combineLatest
import it.unibo.alchemist.model.observables.util.ObservableLists.flatMap
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionDerivedObservableTest {
    private val shared = observe(1)
    private val elements = ObservableMutableList("a", "b")

    private fun sum() = elements.combineLatest(transformer = { shared }, aggregator = { it.sum() })

    private fun <T> Observable<T>.latest(): () -> T? {
        var latest: T? = null
        subscribe { latest = it }
        return { latest }
    }

    @Test
    fun `stopping a combined observable keeps its siblings on the same collection up to date`() {
        val kept = sum().latest()
        sum().subscribe { }.dispose()
        shared.current = 2
        assertEquals(4, kept())
    }

    @Test
    fun `stopping a merged observable keeps its siblings on the same collection up to date`() {
        val kept: () -> Option<Int>? = elements.flatMap { shared }.latest()
        elements.flatMap { shared }.subscribe { }.dispose()
        shared.current = 2
        assertEquals(2.some(), kept())
    }

    @Test
    fun `removing an element keeps observing an observable shared with another element`() {
        val total = sum().latest()
        elements.remove("b")
        shared.current = 2
        assertEquals(2, total())
    }

    @Test
    fun `a combined observable reads the observables it monitors when elements map to fresh ones`() {
        val created = mutableListOf<MutableObservable<Int>>()
        val total = elements
            .combineLatest(transformer = { observe(1).also(created::add) }, aggregator = { it.sum() })
            .latest()
        created.filter { it.observers.isNotEmpty() }.forEach { it.current = 5 }
        assertEquals(10, total())
    }
}
