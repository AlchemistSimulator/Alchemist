/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable

/**
 * Base implementation of a reactive [Condition] owned by [reaction], independent of the kind of host.
 */
abstract class AbstractCondition<T>(override val reaction: Reaction<T>) : Condition<T> {
    private var validity: Observable<Boolean> = observe(true)

    final override val isValid: Observable<Boolean> get() = validity

    override fun dispose() {
        validity.dispose()
    }

    /**
     * Installs the observable backing [isValid].
     *
     * The condition owns the derived view created here, while [newValidity] remains owned by its model source.
     */
    protected fun setValidity(newValidity: Observable<Boolean>) {
        validity.dispose()
        validity = newValidity.map { it }
    }

    override fun toString(): String = this::class.simpleName ?: "<anonymous ${this::class}>"
}
