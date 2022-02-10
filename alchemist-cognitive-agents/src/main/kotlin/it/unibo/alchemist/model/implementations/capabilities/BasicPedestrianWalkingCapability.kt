/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianWalkingCapability

/**
 * Implementation of a basic [PedestrianWalkingCapability].
 */
class BasicPedestrianWalkingCapability @JvmOverloads constructor(
    override val walkingSpeed: Double = Speed.default
) : PedestrianWalkingCapability
