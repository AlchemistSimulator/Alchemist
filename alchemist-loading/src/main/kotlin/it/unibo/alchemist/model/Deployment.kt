/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import java.util.stream.Stream

/**
 * A set of positions where nodes can be deployed.
 *
 * @param P position type
 */
fun interface Deployment<P : Position<out P>> : Iterable<P> {
    /**
     * Returns a [Stream] over the positions of this [Deployment].
     */
    fun stream(): Stream<P>

    override fun iterator(): Iterator<P> = stream().iterator()

    /**
     * Returns the optional [NetworkModel] associated to the deployment, or null if there is none.
     *
     * @param T concentration type of the [NetworkModel]
     */
    fun <T> getAssociatedLinkingRule(): NetworkModel<T>? = null
}
