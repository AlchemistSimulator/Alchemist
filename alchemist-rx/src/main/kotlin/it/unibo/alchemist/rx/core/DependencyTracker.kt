/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.core

import it.unibo.alchemist.rx.core.DependencyTracker.TrackableObservable.currentTrackable
import it.unibo.alchemist.rx.model.observation.Observable
import kotlin.reflect.KProperty

/**
 * Tracks dependencies between observables and computations.
 * It allows defining blocks of code that automatically re-execute when the observables they depend on change.
 */
object DependencyTracker {

    private val contexts = ThreadLocal.withInitial { ArrayDeque<TrackingContext>() }

    private val transactionDepth = ThreadLocal.withInitial { 0 }

    private val pendingReruns = ThreadLocal.withInitial { LinkedHashSet<TrackingContext>() }

    private val currentContext: TrackingContext? get() = contexts.get()?.lastOrNull()

    /**
     * Registers an [observable] as a dependency in the current tracking context.
     * If no tracking context is active, this method does nothing.
     *
     * @param observable the observable to register.
     */
    fun register(observable: Observable<*>) {
        currentContext?.deps?.add(observable)
    }

    /**
     * Executes the given [block] and automatically subscribes to any [Observable] accessed within it.
     * When any of the accessed observables change, the [block] is re-executed.
     *
     * @param owner the object that owns the subscription, used to manage lifecycle.
     * @param block the code block to execute and track.
     */
    fun withAutoSub(owner: Any, block: () -> Unit) {
        lateinit var ctx: TrackingContext

        fun scheduleRerun() {
            if (transactionDepth.get() > 0) pendingReruns.get().add(ctx) else ctx.rerun()
        }

        fun run() {
            ctx.deps.forEach { it.stopWatching(owner) }
            ctx.deps.clear()

            contexts.get().addLast(ctx)
            try {
                block()
            } finally {
                contexts.get().removeLast()
            }

            ctx.deps.forEach { observable ->
                var firstRun = true // avoid initial reschedule
                observable.onChange(owner) {
                    if (!firstRun) scheduleRerun()
                }
                firstRun = false
            }
        }

        ctx = TrackingContext(
            owner = owner,
            deps = mutableSetOf(),
            rerun = ::run,
        )

        run()
    }

    /**
     * Executes the given [block] in a transaction.
     * Re-runs triggered by dependency changes during the transaction are deferred until the transaction completes.
     * This is useful to avoid unnecessary re-computations when multiple dependencies change simultaneously.
     *
     * @param block the code block to execute within the transaction.
     */
    fun transaction(block: () -> Unit) {
        transactionDepth.set(transactionDepth.get() + 1)
        try {
            block()
        } finally {
            val depth = transactionDepth.get() - 1
            transactionDepth.set(depth)

            if (depth == 0) {
                val toRun = pendingReruns.get().toList()
                pendingReruns.get().clear()
                toRun.forEach { it.rerun() }
            }
        }
    }

    private class TrackingContext(val owner: Any, val deps: MutableSet<Observable<*>>, val rerun: () -> Unit)

    /**
     * Extensions for [Observable] to support automatic dependency tracking.
     */
    object TrackableObservable {
        /**
         * Returns the current value of the observable and registers it as a dependency
         * in the current tracking context.
         */
        val <T> Observable<T>.currentTrackable: T
            get() {
                trackRead()
                return current
            }

        /**
         * Registers this observable as a dependency in the current tracking context
         * without returning its value.
         */
        fun <T> Observable<T>.trackRead() {
            register(this)
        }
    }
}

/**
 * Delegate operator to allow using an [Observable] as a property delegate,
 * automatically tracking the dependency.
 *
 * > Note: this is a top level declaration due to limitation in inferring the usage
 * > when importing this extension function due to the resolution of the method
 * > name at compile time. Therefore, in order not to pollute usages with warning suppressions,
 * > this solution simply solves the problem polluting a bit top level declarations for [Observable].
 */
operator fun <T> Observable<T>.getValue(thisRef: Any?, property: KProperty<*>): T = currentTrackable
