/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.server.attributes

import io.ktor.util.AttributeKey
import it.unibo.alchemist.boundary.graphql.util.GraphQLSimulationContext

/**
 * Ktor's attribute to be used for passing a [it.unibo.alchemist.boundary.graphql.util.GraphQLSimulationContext]
 * among Ktor's modules.
 */
val SimulationContextAttributeKey = AttributeKey<GraphQLSimulationContext<*, *>>("simulationContext")
