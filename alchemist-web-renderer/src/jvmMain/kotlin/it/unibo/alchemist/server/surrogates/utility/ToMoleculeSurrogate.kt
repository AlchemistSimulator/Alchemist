/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.surrogates.utility

import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.common.model.surrogate.MoleculeSurrogate

/**
 * A function that maps a [it.unibo.alchemist.model.interfaces.Molecule] to its surrogate class
 * [it.unibo.alchemist.model.surrogate.MoleculeSurrogate]
 * @return the [it.unibo.alchemist.model.surrogate.MoleculeSurrogate] mapped starting from the
 * [it.unibo.alchemist.model.interfaces.Molecule].
 */
fun Molecule.toMoleculeSurrogate(): MoleculeSurrogate = MoleculeSurrogate(name)
