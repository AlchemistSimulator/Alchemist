/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.util

import arrow.core.getOrElse
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node

/**
 * Utilities for the concentrations of biochemical nodes.
 */
internal object Nodes {
    /**
     * The concentration of [molecule] in this node, where a missing molecule has a zero concentration.
     */
    @JvmStatic
    fun Node<Double>.getOrZeroConcentration(molecule: Molecule): Double = this[molecule].getOrElse { 0.0 }
}
