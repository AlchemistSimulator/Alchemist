/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Surrogate class for the [it.unibo.alchemist.model.interfaces.Node] interface.
 * Note: The position is kept by the Environment in the original structure.
 * To improve performance and reduce the serialized [String] size, the position was moved to the node.
 * @param id the id of the [NodeSurrogate].
 * @param contents the mapping between [MoleculeSurrogate] and concentrations.
 * @param position the position of the [NodeSurrogate].
 * @param <TS> the type of concentration surrogate.
 * @param <PS> the type of the [PositionSurrogate].
 */
@Serializable
@SerialName("Node")
data class NodeSurrogate<out TS : Any, out PS : PositionSurrogate>(
    val id: Int,
    val contents: Map<MoleculeSurrogate, TS>,
    val position: PS,
)
