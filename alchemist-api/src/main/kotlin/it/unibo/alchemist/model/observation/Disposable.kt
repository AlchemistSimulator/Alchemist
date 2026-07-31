/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

/**
 * Anything taking up some resource and that can be cleared up.
 */
fun interface Disposable {

    /**
     * Disposes and releases the resources held by this object.
     */
    fun dispose()
}

/**
 * Owns a group of [Disposable] resources and releases them together.
 */
class CompositeDisposable : Disposable {
    private val resources = linkedSetOf<Disposable>()
    private var disposed = false

    /**
     * Adds [resource] to this composite.
     * If this composite was already disposed, [resource] is disposed immediately.
     *
     * @return [resource]
     */
    fun <T : Disposable> add(resource: T): T = resource.also {
        if (disposed) {
            it.dispose()
        } else {
            resources += it
        }
    }

    /**
     * Disposes all currently owned resources while keeping this composite reusable.
     */
    fun clear() {
        val toDispose = resources.toList()
        resources.clear()
        toDispose.forEach(Disposable::dispose)
    }

    override fun dispose() {
        if (!disposed) {
            disposed = true
            clear()
        }
    }
}
