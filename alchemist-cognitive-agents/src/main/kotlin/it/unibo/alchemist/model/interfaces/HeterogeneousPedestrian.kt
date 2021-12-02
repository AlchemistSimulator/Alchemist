/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.HeterogeneousPedestrianModel
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Pedestrians that can differ by age, geneder, etc, depending on their [HeterogeneousPedestrianModel].
 */
interface HeterogeneousPedestrian<T, S : Vector<S>, A : GeometricTransformation<S>> : Pedestrian<T, S, A> {

    /**
     * The pedestrian model, capturing its characteristics.
     */
    val pedestrianModel: HeterogeneousPedestrianModel<T, S, A>
}
