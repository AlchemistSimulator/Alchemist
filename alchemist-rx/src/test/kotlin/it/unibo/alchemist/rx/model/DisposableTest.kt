/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.rx.dsl.ReactiveConditionDSL.condition
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet

class DisposableTest : FunSpec({

    test("Observable map chain disposal") {
        withDisposal {
            val root = observe(1).track()
            val level1 = root.map { it * 2 }.track()
            val level2 = level1.map { it.toString() }.track()

            level2.onChange(this) {}

            root.observers.size shouldBe 1
            level1.observers.size shouldBe 1
            level2.observers.size shouldBe 1

            level2.dispose()

            level1.observers.shouldBeEmpty()
            root.observers.shouldBeEmpty()
        }
    }

    test("ReactiveCondition disposal should unsubscribe from dependencies") {
        withDisposal {
            val dep1 = observe(1).track()
            val dep2 = observe(2).track()

            val cond = condition<Boolean> {
                validity {
                    val v1 by depending(dep1)
                    v1 > 0
                }
                propensity {
                    val v2 by depending(dep2)
                    v2.toDouble()
                }
            }.track()

            // Trigger evaluation to ensure subscriptions are set up
            cond.isValid.onChange(this) { }
            cond.propensityContribution.onChange(this) { }

            dep1.observers.size shouldBe 1
            dep2.observers.size shouldBe 1

            cond.dispose()

            dep1.observers.shouldBeEmpty()
            dep2.observers.shouldBeEmpty()
        }
    }

    test("ObservableSet disposal should clean up derived observables") {
        withDisposal {
            val set = ObservableMutableSet<Int>().track()
            val sizeObs = set.observableSize // derived observable

            sizeObs.onChange(this) {}

            set.add(19)

            set.observers.size shouldBe 1

            set.dispose()
            set.observers.shouldBeEmpty()
        }
    }
}) {
    companion object {
        /**
         * A utility class to track [Disposable] resources and ensure they are disposed.
         */
        class DisposalTracker : Disposable {
            private val resources = mutableListOf<Disposable>()

            /**
             * Registers a [Disposable] to be tracked.
             *
             * @return this [Disposable]
             */
            fun <T : Disposable> T.track(): T = also { resources.add(it) }

            override fun dispose() {
                resources.asReversed().forEach { runCatching { it.dispose() } }
                resources.clear()
            }
        }

        /**
         * Executes the given [block] within a [DisposalTracker] scope.
         * All [Disposable]s tracked via [DisposalTracker.track] will be disposed
         * when the block completes (successfully or exceptionally).
         */
        fun <T> withDisposal(block: DisposalTracker.() -> T): T {
            val tracker = DisposalTracker()
            try {
                return tracker.block()
            } finally {
                tracker.dispose()
            }
        }
    }
}
