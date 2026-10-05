/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.Option

/**
 * Represents an observable object that emits updates to registered observers when its value changes.
 * Subscription changes take effect on later emissions.
 *
 * @param T The type of the value being observed.
 */
interface Observable<T> : Disposable {
    /**
     * The current state or value of the observable.
     */
    val current: T

    /**
     * Represents a list of all entities currently observing changes to this observable object.
     */
    val observers: List<Any>

    /**
     * Stores the set of observers along with the list of callbacks associated with them.
     */
    val observingCallbacks: Map<Any, List<(T) -> Unit>>

    /**
     * Registers a callback to be notified of changes in the observable. The callback is invoked
     * whenever the state of the observable changes.
     *
     * @param registrant The entity registering the callback.
     * @param callback   The callback to be executed when the observable's state changes. It receives
     *                   the updated value of the observable as an argument.
     */
    fun onChange(registrant: Any, callback: (T) -> Unit) = onChange(registrant, true, callback)

    /**
     * Registers a callback to be notified of changes in the observable. The callback is invoked
     * whenever the state of the observable changes.
     *
     * @param registrant The entity registering the callback.
     * @param invokeOnRegistration Whether the callback should be invoked immediately upon registration.
     * @param callback   The callback to be executed when the observable's state changes. It receives
     *                   the updated value of the observable as an argument.
     */
    fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (T) -> Unit)

    /**
     * Subscribes [callback] to this observable using a unique subscription identity.
     *
     * @param invokeOnSubscription whether [callback] should receive [current] immediately
     * @param callback the callback to invoke on updates
     * @return a non-null handle that removes exactly this subscription when disposed
     */
    fun subscribe(invokeOnSubscription: Boolean = true, callback: (T) -> Unit): Disposable {
        val subscription = Any()
        onChange(subscription, invokeOnSubscription, callback)
        var active = true
        return Disposable {
            if (active) {
                active = false
                stopWatching(subscription)
            }
        }
    }

    /**
     * Unregisters the specified registrant from watching for changes or updates in the observable.
     *
     * @param registrant The entity to be unregistered from observing changes.
     */
    fun stopWatching(registrant: Any)

    /**
     * Disposes of the observable by unregistering all currently registered observers.
     * This method ensures that no further updates or notifications are sent to the observers.
     */
    override fun dispose() {
        observers.forEach { this.stopWatching(it) }
    }

    /**
     * Transforms the current observable into a new observable by applying the specified transformation function
     * to its emitted values.
     *
     * @param transform The function to transform each value emitted by this observable.
     * @return A new observable that emits the transformed values.
     */
    fun <S> map(transform: (T) -> S): Observable<S> = object : AbstractObservable<S>() {

        override fun computeFresh(): S = transform(this@Observable.current)

        override fun startMonitoring() = startMonitoring(false)

        override fun startMonitoring(lazy: Boolean) {
            this@Observable.onChange(this, !lazy) {
                updateAndNotify(transform(it))
            }
        }

        override fun stopMonitoring() {
            this@Observable.stopWatching(this)
        }

        override fun toString(): String = "MapObservable($current)[from: ${this@Observable}]"
    }

    /**
     * Combines the current observable with another observable, producing a new observable that emits
     * values derived by applying the provided merge function to the current values of both observables.
     *
     * @param other The observable to be merged with the current observable.
     * @param merge The function that combines the values from the two observables into a single value.
     * @return A new observable that emits values resulting from merging the two observables.
     */
    fun <O, R> mergeWith(other: Observable<O>, merge: (T, O) -> R): Observable<R> = object : AbstractObservable<R>() {

        override fun computeFresh(): R = merge(this@Observable.current, other.current)

        override fun startMonitoring() = startMonitoring(false)

        override fun startMonitoring(lazy: Boolean) {
            val handleUpdate: (Any?) -> Unit = {
                updateAndNotify(merge(this@Observable.current, other.current))
            }

            listOf(this@Observable, other).forEach { obs ->
                obs.onChange(this, !lazy, handleUpdate)
            }
        }

        override fun stopMonitoring() {
            listOf(this@Observable, other).forEach { it.stopWatching(this) }
        }

        override fun toString() = "MergeObservable($current)[from: ${this@Observable}, other: $other]"
    }

    /**
     * Set of handy extension methods for Observables.
     */
    companion object ObservableExtensions {

        /**
         * Retrieves the current value of the observable if present, or `null` if no value is available.
         *
         * @return The current value of the observable, or `null` if it is not set.
         */
        fun <T> Observable<Option<T>>.currentOrNull(): T? = current.getOrNull()

        /**
         * Converts this [Observable] into a [MutableObservable] of the same type [T].
         * If this observable is already mutable, this observable is returned; however,
         * if it does not, a new, separate observable will be created, therefore, no
         * observers are shared between this and the returned observable.
         *
         * @return a mutable version of this observable.
         */
        fun <T> Observable<T>.asMutable(): MutableObservable<T> =
            this as? MutableObservable<T> ?: object : MutableObservable<T> {
                override var current: T = this@asMutable.current
                    get() = this@asMutable.current

                override var observers: List<Any> = emptyList()

                override val observingCallbacks: MutableMap<Any, List<(T) -> Unit>> = mutableMapOf()

                override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (T) -> Unit) {
                    observers += registrant
                    observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
                    this@asMutable.onChange(this to registrant, invokeOnRegistration, callback)
                }

                override fun stopWatching(registrant: Any) {
                    observers -= registrant
                    observingCallbacks.remove(registrant)
                    this@asMutable.stopWatching(this to registrant)
                }
            }
    }
}
