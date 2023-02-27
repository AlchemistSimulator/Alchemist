/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.serialization

import it.unibo.alchemist.common.model.surrogate.EmptyConcentrationSurrogate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Object containing the [SerializersModule]s used by the web-renderer project.
 */
object SerializationModules {

    /**
     * The [SerializersModule] used to serialize and deserialize all the possible Concentration types.
     */
    @OptIn(ExperimentalSerializationApi::class)
    val concentrationModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(EmptyConcentrationSurrogate::class)
            defaultDeserializer { EmptyConcentrationSurrogate.serializer() }
        }
    }
}
