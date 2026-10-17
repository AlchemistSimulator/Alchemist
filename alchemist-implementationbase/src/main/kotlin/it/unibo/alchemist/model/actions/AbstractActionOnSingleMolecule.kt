/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction

/**
 * A node-bound action that modifies the concentration of a single [molecule].
 *
 * @param T concentration type
 */
abstract class AbstractActionOnSingleMolecule<T>(
    reaction: NodeReaction<T>,
    /**
     * The molecule whose concentration is modified by this action.
     */
    val molecule: Molecule,
) : AbstractLocalAction<T>(reaction)
