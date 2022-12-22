/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Surrogate class for the [it.unibo.alchemist.model.interfaces.Environment] interface.
 * @param dimensions the number of dimensions of the [EnvironmentSurrogate].
 * @param nodes the nodes contained in the [EnvironmentSurrogate].
 * @param <TS> the type of concentration.
 * @param <PS> the type of [PositionSurrogate].
 */
@Serializable
@SerialName("Environment")
data class EnvironmentSurrogate<out TS : Any, out PS : PositionSurrogate>(
    val dimensions: Int,
    val nodes: List<NodeSurrogate<TS, PS>>
) {
    companion object {
        /**
         * @param <TS> the type of concentration.
         * @param <PS> the type of [PositionSurrogate].
         * @return an empty and uninitialized [EnvironmentSurrogate].
         */
        fun <TS : Any, PS : PositionSurrogate> uninitializedEnvironment(): EnvironmentSurrogate<TS, PS> =
            EnvironmentSurrogate(-1, emptyList())

        /**
         * @return The most general polymorphic serializer for the [EnvironmentSurrogate] class, using [Any] and
         * [PositionSurrogate] as type parameters.
         */
        fun polymorphicSerializer(): KSerializer<EnvironmentSurrogate<Any, PositionSurrogate>> = serializer(
            PolymorphicSerializer(Any::class),
            PositionSurrogate.serializer()
        )
    }
}
