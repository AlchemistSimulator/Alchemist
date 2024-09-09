/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position

/**
 * Provides support for rules that connect nodes within a given [radius] and through special nodes marked as
 * access points, recognized through a [Molecule] [accessPointId].
 */
abstract class AbstractAccessPointRule<T, P : Position<P>>(
    val radius: Double,
    val accessPointId: Molecule,
) : ConnectWithinDistance<T, P>(radius) {

    // TODO: this is a memory leak and should be fixed once the set of nodes is observable
    private val accessPoints = mutableMapOf<Node<T>, Boolean>()

    /**
     * Returns true if the node is an access point.
     */
    protected val Node<T>.isAccessPoint: Boolean get() = accessPoints.getOrPut(this) {
        contains(accessPointId).onChange(this@AbstractAccessPointRule) {
            accessPoints[this] = it
        }
        accessPoints.getValue(this)
    }
}
