/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

/**
 * Represents an observable object that emits updates to registered observers when its value changes.
 * Registration changes take effect on later emissions.
 *
 * Observers are notified as soon as the value changes, while other observables depending on the same change may not
 * have been updated yet.
 * A callback must therefore not rely on state it does not observe:
 * a result depending on several observables must observe all of them, so that it is recomputed once each is updated.
 *
 * @param T The type of the value being observed.
 */
interface Observable<out T> : Disposable {
    /**
     * The current state or value of the observable.
     */
    val current: T

    /**
     * Whether any callback is currently registered.
     */
    val isObserved: Boolean get() = observerCount > 0

    /**
     * How many callbacks are currently registered.
     */
    val observerCount: Int

    /**
     * Unregisters every callback registered with this observable.
     */
    override fun dispose()

    /**
     * Transforms the current observable into a new observable by applying the specified transformation function
     * to its emitted values.
     *
     * @param transform The function to transform each value emitted by this observable.
     * @return A new observable that emits the transformed values.
     */
    fun <S> map(transform: (T) -> S): Observable<S>

    /**
     * Combines the current observable with another observable, producing a new observable that emits
     * values derived by applying the provided merge function to the current values of both observables.
     *
     * @param other The observable to be merged with the current observable.
     * @param merge The function that combines the values from the two observables into a single value.
     * @return A new observable that emits values resulting from merging the two observables.
     */
    fun <O, R> mergeWith(other: Observable<O>, merge: (T, O) -> R): Observable<R>

    /**
     * Registers [callback] to be invoked with every new value of this observable, and right away with the current one
     * if [notifyOnRegistration].
     *
     * @return a handle unregistering [callback]; disposing it more than once has no effect
     */
    fun onChange(notifyOnRegistration: Boolean = true, callback: (T) -> Unit): Disposable
}
