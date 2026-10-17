/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observation.Observable

/**
 * An [Environment] supporting deformable cells.
 *
 * @param P position type
 */
interface EnvironmentSupportingDeformableCells<P : Position<out P>> : Environment<Double, P> {
    /**
     * The biggest diameter among the unstressed deformable cells, or zero if there are none.
     * It changes as cells are added and removed, which changes the range within which deformable cells interact.
     */
    val maxDiameterAmongCircularDeformableCells: Observable<Double>
}
