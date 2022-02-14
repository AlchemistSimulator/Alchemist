/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.capabilities

import it.unibo.alchemist.model.interfaces.Capability
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Node

/**
 * The pedestrian's capability for form groups.
 */
interface PedestrianSocialCapability<T> : Capability {
    /**
     * Pedestrian's [Group]
     */
    val group: Group<T, Node<T>>
}
