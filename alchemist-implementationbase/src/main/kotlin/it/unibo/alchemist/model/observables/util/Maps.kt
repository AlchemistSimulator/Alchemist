/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables.util

internal fun <T> Map<Any, List<(T) -> Unit>>.notifyCurrentObservers(value: T) {
    // Snapshot both registrants and callbacks so subscriptions may change, or emit again, during notification.
    values.flatten().forEach { it(value) }
}
