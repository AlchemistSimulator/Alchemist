/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.Observable

/**
 * The base implementation of [Observable]: it keeps the registered callbacks, hands out their handles, and notifies
 * them of the values published through [updateAndNotify].
 *
 * Observables holding their own state publish it whenever it changes. Observables derived from other sources compute
 * their value through [computeFresh], and observe their sources, through [observeSource], only while they are observed.
 *
 * @param emitOnDistinct whether to emit only when the new derived value is different from the current one.
 * If so, the first registration computes the current value even when not delivering it, so that the first emission
 * can be compared against it; otherwise, a registration that does not deliver the current value computes nothing.
 * @param T The type of data being observed.
 */
abstract class AbstractObservable<T>(protected val emitOnDistinct: Boolean = true) : Observable<T> {
    /**
     * The current value: while observed, the latest value published; otherwise, a freshly computed one.
     * Observables holding their own state may read it directly.
     */
    override val current: T
        get() = if (isListening) cached.getOrElse { computeFresh() } else computeFresh()

    final override val observerCount: Int get() = callbacks.size

    protected var cached: Option<T> = none()

    // The registered callbacks, by the handle of their registration.
    private val callbacks = LinkedHashMap<Disposable, (T) -> Unit>()

    // Counts the emissions, so that a notification can tell whether a callback emitted again.
    private var emissions = 0L

    private var isListening = false

    // The observation of the sources started by the first observer, null while the sources are not observed.
    private var session: SourcesObservation? = null

    override fun dispose() {
        if (isListening) {
            stopListening()
        }
        callbacks.clear()
    }

    override fun <S> map(transform: (T) -> S): Observable<S> = object : AbstractObservable<S>() {
        override fun computeFresh(): S = transform(this@AbstractObservable.current)

        override fun startObservingSources() {
            observeSource(this@AbstractObservable) { updateAndNotify(transform(it)) }
        }

        override fun toString(): String = "MapObservable($current)[from: ${this@AbstractObservable}]"
    }

    override fun <O, R> mergeWith(other: Observable<O>, merge: (T, O) -> R): Observable<R> =
        object : AbstractObservable<R>() {
            override fun computeFresh(): R = merge(this@AbstractObservable.current, other.current)

            override fun startObservingSources() {
                val handleUpdate: (
                    Any?,
                ) -> Unit = { updateAndNotify(merge(this@AbstractObservable.current, other.current)) }
                listOf(this@AbstractObservable, other).forEach { observeSource(it, handleUpdate) }
            }

            override fun toString() = "MergeObservable($current)[from: ${this@AbstractObservable}, other: $other]"
        }

    override fun onChange(notifyOnRegistration: Boolean, callback: (T) -> Unit): Disposable {
        val wasEmpty = !isObserved
        // An object rather than a lambda: the handle removes itself, and in a lambda `this` is the observable.
        val handle = object : Disposable {
            override fun dispose() {
                if (callbacks.remove(this) != null && !isObserved && isListening) {
                    stopListening()
                }
            }
        }
        callbacks[handle] = callback
        if (wasEmpty) {
            isListening = true
            session = SourcesObservation()
            if (notifyOnRegistration || emitOnDistinct) {
                /*
                 * Distinct observables cache the initial value even when not delivering it,
                 * so that the first emission is compared against it.
                 */
                val initial = computeFresh()
                cached = initial.some()
                /*
                 * Starting to observe the sources may itself publish a value:
                 * subscribing to the sources can make them, or this observable, emit.
                 * Such an emission is notified to every registered callback, including the one just added,
                 * and is newer than initial, which was computed before the sources were observed.
                 * Delivering initial afterwards would overwrite it with an outdated value.
                 * The emission count is therefore recorded before observing the sources and compared right after:
                 * if it changed, the callback already received a newer value, and initial is not delivered.
                 */
                val emissionsBeforeObservingSources = emissions
                // The sources are observed before notifying, not to miss source changes made by the callback itself.
                startObservingSources()
                if (notifyOnRegistration && emissions == emissionsBeforeObservingSources) {
                    callback(initial)
                }
            } else {
                startObservingSources()
            }
        } else if (notifyOnRegistration) {
            callback(current)
        }
        return handle
    }

    /**
     * Computes and returns a fresh value of type [T]. This method is expected to be implemented
     * in derived classes to provide a new value that represents the updated state or computation
     * result of the observable entity.
     *
     * @return a fresh, computed value of type [T]
     */
    protected abstract fun computeFresh(): T

    /**
     * Registers [callback] with [source] for as long as this observable observes its sources, without notifying the
     * current value of [source]: the registration is released when the sources stop being observed, or right away if
     * they stopped while registering, as callbacks run during the registration may make this observable unobserved.
     *
     * @return a handle releasing the registration earlier, e.g., when [source] stops being a source
     */
    protected fun <S> observeSource(source: Observable<S>, callback: (S) -> Unit): Disposable {
        val registeringSession = session
        val handle = source.onChange(notifyOnRegistration = false, callback)
        return when {
            // Each session has its own list of registrations: the session is still the same if the list is.
            registeringSession != null && session?.registrations === registeringSession.registrations -> {
                registeringSession.registrations += handle
                // Disposing the registration and leaving the session are idempotent, as disposing a handle must be.
                Disposable {
                    handle.dispose()
                    registeringSession.registrations -= handle
                }
            }
            else -> {
                handle.dispose()
                Disposable { }
            }
        }
    }

    /**
     * Starts observing the sources of this observable, publishing their changes through [updateAndNotify].
     * It is called when the first observer registers, after the current value has been computed if needed:
     * implementations register with their sources through [observeSource]. Observables holding their own state have no
     * sources, and this implementation observes nothing.
     */
    protected open fun startObservingSources() = Unit

    /**
     * Stops observing the sources of this observable, after the registrations made through [observeSource] have been
     * released. Implementations override it to release any further state kept while observing them.
     */
    protected open fun stopObservingSources() = Unit

    /**
     * Publishes [newValue] to the registered callbacks, if it differs from the latest one or if every value is emitted.
     * The callbacks are snapshotted, so that registrations may change during the notification. A callback may also
     * publish a newer value, whose nested notification reaches every callback: the remaining ones must not receive
     * the outdated [newValue] afterwards, so the notification stops as soon as a newer one starts.
     */
    protected fun updateAndNotify(newValue: T) {
        val hasChanged = cached.fold(
            ifEmpty = { true },
            ifSome = { it != newValue },
        )
        if (!emitOnDistinct || hasChanged) {
            cached = newValue.some()
            val emission = ++emissions
            // The sequence is lazy, so that the emission is checked after each callback rather than all upfront.
            callbacks.values.toList().asSequence().takeWhile { emissions == emission }.forEach { it(newValue) }
        }
    }

    private fun stopListening() {
        val ended = session
        session = null
        ended?.registrations?.toList()?.forEach(Disposable::dispose)
        stopObservingSources()
        isListening = false
        cached = none()
    }

    // The registrations made through observeSource, released together when the sources stop being observed.
    @JvmInline
    private value class SourcesObservation(val registrations: MutableList<Disposable> = mutableListOf<Disposable>())
}
