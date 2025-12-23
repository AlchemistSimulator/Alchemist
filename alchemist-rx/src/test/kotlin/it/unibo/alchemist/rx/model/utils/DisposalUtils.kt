/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.utils

import it.unibo.alchemist.rx.model.Disposable

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
