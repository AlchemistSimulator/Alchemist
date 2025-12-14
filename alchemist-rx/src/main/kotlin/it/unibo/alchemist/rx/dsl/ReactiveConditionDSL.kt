/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.dsl

import it.unibo.alchemist.rx.model.ReactiveCondition
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.filter
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.union
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet
import it.unibo.alchemist.rx.model.observation.ObservableSet
import kotlin.properties.ReadOnlyProperty

/**
 * Simple DSL for creating a reactive condition which depends on a set of [observables][Observable].
 * The usage of such DSL is encouraged due to:
 * 1. Automatic re-evaluation of the condition when one of the dependencies changes
 * 2. Automatic wiring to observables and possibly even
 * 3. The unsubscription from observables if the body re-evaluation detected that a dependency is no more accessed.
 *
 * In the context of Alchemist dependencies are somehow static once a condition is defined; therefore,
 * the 'stale dependencies' check will just consist of empty sets checks with minimal overhead.
 *
 * The following example exemplifies the usage of the DSL for building a condition that simply returns
 * true if a target molecule is contained in the host node, and it has a valid concentration associated.
 *
 * ```kotlin
 *fun <T> containsMolecule(node: ObservableNode<T>, target: Molecule): ReactiveCondition<T> =
 *     condition<T> {
 *         validity {
 *             val x by depending(node.observeConcentration(target))
 *             x.isSome()
 *         }
 *         propensity { if (it) 1.0 else 2.0 }
 *     }
 * }
 * ```
 *
 */
object ReactiveConditionDSL {

    /**
     * DSL entrypoint.
     */
    fun <T> condition(configure: ConditionBuilder.() -> Unit): ReactiveCondition<T> {
        val builder = ConditionBuilder().apply(configure)

        val validityBlock = requireNotNull(builder.validityBlock) {
            "Error constructing condition: the validity block must be specified." +
                "You can build one specifying a `validity { }` block inside a `condition` block."
        }.let { ReactiveConditionContainer(builder.declaredDependencies, it) }

        val propensityBlock = requireNotNull(builder.propensityBlock) {
            "Error constructing condition: the propensity block must be specified." +
                "You can build one specifying a `propensity { }` block inside a `condition` block."
        }.let {
            ReactiveConditionContainer {
                val valid by depending(validityBlock)
                it(valid)
            }
        }

        return object : ReactiveCondition<T> {

            override val isValid: Observable<Boolean> = validityBlock

            override val propensityContribution: Observable<Double> = propensityBlock

            override val observableInboundDependencies: ObservableSet<Observable<*>> =
                validityBlock.observableDeps union propensityBlock.observableDeps.filter { it != validityBlock }

            override fun toString(): String =
                "ReactiveCondition(isValid=${isValid.current}, propensity=${propensityContribution.current})"
        }
    }

    /**
     * A simple utility for storing and adding dependencies as [Observable] in the
     * context of the condition DSL builder.
     */
    class ConditionDSL {
        internal val dependencies = mutableSetOf<Observable<*>>()

        /**
         * Keeps track of the last sent value of an observable.
         */
        internal var pushedValues: Map<Observable<*>, Any?> = emptyMap()

        /**
         * Specify a dependency through the given [Observable]. The result provided
         * should be used in conjunction with property delegate as:
         * ```kotlin
         * condition {
         *     validity {
         *         val value: T by depending(node.observeConcentration(target))
         *         ...
         *     }
         * }
         * ```
         * This method takes care of extracting the emitted value if some, and
         *  provides the receiver with it.
         */
        fun <T> depending(observable: Observable<T>): ReadOnlyProperty<Any?, T> {
            dependencies.add(observable)
            return ReadOnlyProperty { _, _ ->
                @Suppress("UNCHECKED_CAST")
                if (pushedValues.containsKey(observable)) {
                    pushedValues[observable] as T
                } else {
                    observable.current
                }
            }
        }

        /**
         * Manually declares a dependency on an [observable].
         * This forces the condition to re-evaluate when the [observable] changes,
         * even if its value is not explicitly used via [depending].
         * Should be quite useful for global dependencies.
         */
        fun dependsOn(observable: Observable<*>) {
            dependencies.add(observable)
        }
    }

    /**
     * Condition builder for this DSL, enabling the creation of [ReactiveCondition]s
     * through the [validity] and [propensity] blocks.
     */
    class ConditionBuilder {
        internal var validityBlock: (ConditionDSL.() -> Boolean)? = null
        internal var propensityBlock: (ConditionDSL.(Boolean) -> Double)? = null
        internal val declaredDependencies = mutableListOf<Observable<*>>()

        /**
         * Specify this [condition][ReactiveCondition] validity block.
         */
        fun validity(block: ConditionDSL.() -> Boolean) {
            validityBlock = block
        }

        /**
         * Specify this [condition][ReactiveCondition] propensity contribution
         * based on this condition's current validity.
         */
        fun propensity(block: ConditionDSL.(Boolean) -> Double) {
            propensityBlock = block
        }

        /**
         * Adds a static dependency to the condition.
         * The condition will re-evaluate whenever the given [Observable] changes.
         * This is useful for plugging global dependencies or dependencies that
         * could not have been declared through [ConditionDSL.depending] construct.
         */
        fun dependsOn(observable: Observable<*>) {
            declaredDependencies.add(observable)
        }
    }

    private class ReactiveConditionContainer<T>(
        private val declaredDependencies: List<Observable<*>> = emptyList(),
        private val block: ConditionDSL.() -> T,
    ) : Observable<T> {

        private val dsl = ConditionDSL()
        private val callbacks = mutableMapOf<Any, (T) -> Unit>()
        private val latestValues = mutableMapOf<Observable<*>, Any?>()

        private val observingDeps = mutableSetOf<Observable<*>>()

        val observableDeps = ObservableMutableSet<Observable<*>>()

        // used to skipping initial callbacks call when subscribing
        private var initialized = false

        override var current: T = compute().also { initialized = true }
        override var observers: List<Any> = emptyList()

        override fun onChange(registrant: Any, callback: (T) -> Unit) {
            callbacks[registrant] = callback
            observers += registrant
            callback(current)
        }

        override fun stopWatching(registrant: Any) {
            callbacks.remove(registrant)
            observers -= registrant
        }

        override fun dispose() {
            observingDeps.forEach { it.stopWatching(this) }
            observingDeps.clear()
            callbacks.clear()
            observers = emptyList()
        }

        private fun compute(): T {
            dsl.dependencies.clear()
            dsl.dependencies.addAll(declaredDependencies)
            dsl.pushedValues = latestValues
            val result = dsl.block()
            updateDependencies(dsl.dependencies)
            return result
        }

        /**
         * Stale dependencies check. Consider the following case:
         *
         * ```kotlin
         * condition {
         *     validity {
         *         val mode by depending(modeObservable)
         *         if (mode == "A") {
         *             val x by depending(observableA) // Dependency on A
         *             x > 10
         *         } else {
         *             val x by depending(observableB) // Dependency on B
         *             x < 5
         *         }
         *     }
         * }
         *```
         * With this check we could dynamically subscribe and unsubscribe from dependencies which
         * are no more accessed during the re-evaluation of this condition's block.
         * In Alchemist conditions are somehow static once a condition is defined, however this could
         * lead to more sophisticated dependencies behaviours while in the same time supporting current
         * Alchemist's behaviour with minimal overhead.
         */
        private fun updateDependencies(newDependencies: Set<Observable<*>>) {
            val toRemove = observingDeps - newDependencies
            val toAdd = newDependencies - observingDeps

            toRemove.forEach {
                it.stopWatching(this)
                if (it in observableDeps) {
                    observableDeps.remove(it)
                }
                latestValues.remove(it)
            }
            toAdd.forEach { observable ->
                observableDeps.add(observable)
                var firstRun = true
                observable.onChange(this) { newValue ->
                    latestValues[observable] = newValue
                    if (!firstRun) { // skipping potential reevaluation when subscribing
                        reevaluate()
                    }
                }
                firstRun = false
            }
            observingDeps.clear()
            observingDeps.addAll(newDependencies)
        }

        private fun reevaluate() {
            if (!initialized) return
            val newValue = compute()
            if (newValue != current) {
                current = newValue
                callbacks.values.forEach { it(newValue) }
            }
        }

        override fun toString(): String = "ReactiveConditionContainer(current=$current)"
    }
}
