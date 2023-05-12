/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.surrogates.utility

import it.unibo.alchemist.boundary.webui.common.model.surrogate.EmptyConcentrationSurrogate

/**
 * A set of functions to map the concentrations to the corresponding surrogate classes.
 */
object ToConcentrationSurrogate {

    /**
     * @return A function that maps any object to an [EmptyConcentrationSurrogate].
     */
    val toEmptyConcentration: (Any) -> EmptyConcentrationSurrogate = {
        EmptyConcentrationSurrogate
    }
}
