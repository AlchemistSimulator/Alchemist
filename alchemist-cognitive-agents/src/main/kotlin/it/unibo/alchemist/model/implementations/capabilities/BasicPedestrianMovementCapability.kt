/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.interfaces.capabilities.PedestrianMovementCapability

/**
 * Implementation of a basic [PedestrianMovementCapability]
 */
class BasicPedestrianMovementCapability(
    override val walkingSpeed: Double = BasicPedestrianWalkingCapability().walkingSpeed,
    override val runningSpeed: Double = BasicPedestrianRunningCapability().runningSpeed
) : PedestrianMovementCapability
