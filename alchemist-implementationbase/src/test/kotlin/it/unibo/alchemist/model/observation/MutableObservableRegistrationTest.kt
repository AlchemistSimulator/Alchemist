/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import kotlin.test.Test
import kotlin.test.assertEquals

class MutableObservableRegistrationTest {
    private val observable = observe(0)
    private val received = mutableListOf<Int>()

    @Test
    fun `a change made by the initial callback is notified to the same callback`() {
        observable.onChange { value ->
            received += value
            if (value == 0) {
                observable.current = 1
            }
        }
        assertEquals(listOf(0, 1), received)
    }

    @Test
    fun `a callback can unsubscribe itself`() {
        lateinit var subscription: Disposable
        subscription = observable.onChange(notifyOnRegistration = false) { value ->
            received += value
            subscription.dispose()
        }
        observable.current = 1
        observable.current = 2
        assertEquals(listOf(1), received)
        assertEquals(0, observable.observerCount)
    }
}
