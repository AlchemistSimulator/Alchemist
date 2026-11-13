/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import kotlinx.collections.immutable.toImmutableList

/**
 * Turns Alchemist nodes into what the viewport draws.
 */
internal object AlchemistNodes {
    /** This node as the viewport shows it, at its position in [environment]. */
    fun <T, P : Position<P>> Node<T>.toViewport(environment: Environment<T, P>): ViewportNode = ViewportNode(
        id = id,
        coordinates = environment.currentPositionOf(this).coordinates.toList().toImmutableList(),
        concentrations = contents.current
            .map { (molecule, concentration) -> Labeled(molecule.toString(), concentration) }
            .toImmutableList(),
    )
}
