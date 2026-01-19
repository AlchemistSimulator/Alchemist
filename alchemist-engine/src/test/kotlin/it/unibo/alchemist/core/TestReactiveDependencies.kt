/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import java.util.concurrent.atomic.AtomicInteger

class TestReactiveDependencies : AbstractDependencyTest() {

    private lateinit var environment: Environment<Double, *>

    override fun beforeTest(environment: Environment<Double, *>) {
        this.environment = environment
    }

    override fun Reaction<Double>.assertDependencies(vararg dependencies: Reaction<Double>) {
        val counters = dependencies.associateWith { AtomicInteger(0) }

        dependencies.forEach { target ->
            target.initializationComplete(Time.ZERO, environment)
            target.rescheduleRequest.onChange(this) {
                counters[target]?.incrementAndGet()
            }
        }
        this.execute()

        dependencies.forEach { target ->
            val count = counters[target]?.get()
            count.shouldNotBeNull()
            count shouldBeGreaterThan 0
        }

        dependencies.forEach { it.rescheduleRequest.stopWatching(this) }
    }
}
