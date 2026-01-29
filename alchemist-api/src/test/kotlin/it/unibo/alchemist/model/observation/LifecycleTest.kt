/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.observation.LifecycleState.DESTROYED
import it.unibo.alchemist.model.observation.LifecycleState.STARTED
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe

class LifecycleTest : FunSpec({

    class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry()
        override val lifecycle: Lifecycle get() = registry

        fun start() = registry.markState(STARTED)
        fun destroy() = registry.markState(DESTROYED)
    }

    context("Lifecycle binding tests") {

        test("bindTo should not invoke callback if Lifecycle is INITIALIZED") {
            val owner = TestLifecycleOwner()
            val observable = observe(10)
            var lastValue = -1

            observable.bindTo(owner) { lastValue = it }

            lastValue shouldBe -1
            observable.current = 20
            lastValue shouldBe -1
        }

        test("bindTo should invoke callback immediately if Lifecycle is STARTED") {
            val owner = TestLifecycleOwner()
            owner.start()
            val observable = observe(10)
            var lastValue = -1

            observable.bindTo(owner) { lastValue = it }

            lastValue shouldBe 10
        }

        test("bindTo should start receiving updates when Lifecycle moves to STARTED") {
            val owner = TestLifecycleOwner()
            val observable = observe(10)
            var lastValue = -1

            observable.bindTo(owner) { lastValue = it }

            observable.current = 20
            lastValue shouldBe -1

            owner.start()
            lastValue shouldBe 20

            observable.current = 30
            lastValue shouldBe 30
        }

        test("bindTo should automatically unsubscribe when Lifecycle is DESTROYED") {
            val owner = TestLifecycleOwner()
            owner.start()
            val observable = observe(10)
            var lastValue = -1

            observable.bindTo(owner) { lastValue = it }

            observable.observers shouldContain owner

            owner.destroy()

            observable.observers shouldNotContain owner

            observable.current = 40
            lastValue shouldBe 10
        }

        test("bindTo should not subscribe at all if Lifecycle is already DESTROYED") {
            val owner = TestLifecycleOwner()
            owner.destroy()
            val observable = observe(10)

            observable.bindTo(owner) { }

            observable.observers shouldNotContain owner
        }

        test("Multiple bindings to the same owner should work independently") {
            val owner = TestLifecycleOwner()
            owner.start()
            val obs1 = observe(1)
            val obs2 = observe(2)
            var val1 = -1
            var val2 = -1

            obs1.bindTo(owner) { val1 = it }
            obs2.bindTo(owner) { val2 = it }

            val1 shouldBe 1
            val2 shouldBe 2

            owner.destroy()

            obs1.observers shouldNotContain owner
            obs2.observers shouldNotContain owner
        }
    }
})
