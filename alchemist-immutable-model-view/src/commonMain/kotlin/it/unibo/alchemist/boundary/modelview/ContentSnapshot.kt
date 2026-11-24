/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlinx.serialization.Serializable

/**
 * A molecule in a node.
 *
 * @property molecule the molecule name.
 * @property value the concentration.
 * @property type the type of the concentration in the simulation, which [value] does not keep.
 */
@Serializable
data class ContentSnapshot(
    val molecule: String,
    val value: ValueSnapshot,
    val type: String,
)
