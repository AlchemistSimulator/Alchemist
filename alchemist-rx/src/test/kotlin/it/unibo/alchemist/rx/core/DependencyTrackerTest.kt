/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.rx.core.DependencyTracker.TrackableObservable.currentTrackable
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe

class DependencyTrackerTest : FunSpec({

    test("withAutoSub should re-execute block when dependencies change") {
        val observable = observe(1)
        var value = 0

        DependencyTracker.withAutoSub(this) {
            value = observable.currentTrackable
        }

        value shouldBe 1
        observable.update { it + 1 }
        value shouldBe 2
    }

    test("withAutoSub should handle dynamic dependencies") {
        val switch = observe(true)
        val obsA = observe("A")
        val obsB = observe("B")
        var result = ""

        var recomputeCount = 0
        DependencyTracker.withAutoSub(this) {
            result = if (switch.currentTrackable) obsA.currentTrackable else obsB.currentTrackable
            recomputeCount++
        }

        result shouldBe "A"
        recomputeCount shouldBe 1

        obsA.current = "A2"
        result shouldBe "A2"
        recomputeCount shouldBe 2

        switch.current = false
        result shouldBe "B"
        recomputeCount shouldBe 3

        obsA.current = "A3"
        result shouldBe "B"
        recomputeCount shouldBe 3

        obsB.current = "B2"
        result shouldBe "B2"
        recomputeCount shouldBe 4
    }

    test("transaction should defer updates until completion") {
        val obs = observe(0)
        var runCount = 0

        DependencyTracker.withAutoSub(this) {
            obs.currentTrackable
            runCount++
        }

        runCount shouldBe 1

        DependencyTracker.transaction {
            obs.current = 1
            obs.current = 2
            obs.current = 3
        }

        runCount shouldBe 2
        obs.current shouldBe 3
    }

    test("nested transactions should work correctly") {
        val obs = observe(0)
        var runCount = 0

        DependencyTracker.withAutoSub(this) {
            obs.currentTrackable
            runCount++
        }

        runCount shouldBe 1

        DependencyTracker.transaction {
            obs.current = 1
            DependencyTracker.transaction {
                obs.current = 2
            }
            obs.current = 3
        }

        runCount shouldBe 2
        obs.current shouldBe 3
    }

    test("property delegation should work with tracking") {
        val obs = observe("initial")
        var captured = ""

        DependencyTracker.withAutoSub(this) {
            val v by obs
            captured = v
        }

        captured shouldBe "initial"
        obs.current = "updated"
        captured shouldBe "updated"
    }
})
