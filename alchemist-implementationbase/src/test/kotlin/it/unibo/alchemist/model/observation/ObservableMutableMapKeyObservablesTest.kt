/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observables.ObservableMutableMap
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableMutableMapKeyObservablesTest {
    private val map = ObservableMutableMap(mapOf("initial" to 1))

    private fun stopObservingTheMap() = map.subscribe { }.dispose()

    @Test
    fun `stopping a map observer keeps the values of initial keys observable`() {
        stopObservingTheMap()
        assertEquals(1.some(), map["initial"].current)
    }

    @Test
    fun `stopping a map observer keeps the values of added keys observable`() {
        map.put("added", 2)
        stopObservingTheMap()
        assertEquals(2.some(), map["added"].current)
    }

    @Test
    fun `stopping a map observer keeps removed keys absent`() {
        map["initial"].subscribe { }.dispose()
        map.remove("initial")
        stopObservingTheMap()
        assertEquals(none(), map["initial"].current)
    }
}
