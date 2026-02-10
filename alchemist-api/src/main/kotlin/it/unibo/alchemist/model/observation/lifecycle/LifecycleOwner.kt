/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation.lifecycle

import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.lifecycle.LifecycleState.DESTROYED
import it.unibo.alchemist.model.observation.lifecycle.LifecycleState.STARTED

/**
 * An object which has a [Lifecycle].
 */
interface LifecycleOwner {

    /**
     * The lifecycle of the provider.
     */
    val lifecycle: Lifecycle
}

/**
 * Observes this [it.unibo.alchemist.model.observation.Observable] within the context of a [LifecycleOwner]. This
 * method is a memory-safe alternative to [it.unibo.alchemist.model.observation.Observable.onChange] in scenarios
 * where it is crucial to avoid that the owner subscription leaks. When the
 * registrant's state reaches [LifecycleState.DESTROYED], the subscription
 * is automatically removed and cleared up. Moreover, the [callback] is only
 * invoked if the lifecycle is in an active state ([STARTED]). Finally,
 * when the lifecycle moves from an inactive state back to active, the
 * [callback] is triggered immediately with the [it.unibo.alchemist.model.observation.Observable.current] value.
 *
 * @param lifecycleOwner The object controlling the lifecycle of this subscription.
 * @param callback The action to perform when the observable emits a value.
 * @return a [it.unibo.alchemist.model.observation.Disposable]
 *         to manually dispose the subscription outside owner's lifecycle.
 */
fun <T> Observable<T>.bindTo(lifecycleOwner: LifecycleOwner, callback: (T) -> Unit): Disposable? =
    lifecycleOwner.takeIf { it.lifecycle.currentState != DESTROYED }?.let {
        val dataListener: (T) -> Unit = { data ->
            // avoid zombie callbacks
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(STARTED)) {
                callback(data)
            }
        }

        var lifecycleObserver: ((LifecycleState) -> Unit)? = null
        lifecycleObserver = { state ->
            when (state) {
                DESTROYED -> {
                    this.stopWatching(lifecycleOwner)
                    lifecycleObserver?.let { lifecycleOwner.lifecycle.removeObserver(it) }
                }
                STARTED -> callback(this.current)
                else -> { /* No action needed for other states */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // set `invokeOnRegistration = false` because we handle the initial data emission manually below
        // to strictly respect the `isAtLeast(STARTED)` check.
        this.onChange(lifecycleOwner, invokeOnRegistration = false, callback = dataListener)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(STARTED)) {
            callback(this.current)
        }

        return Disposable {
            this@bindTo.stopWatching(dataListener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }
