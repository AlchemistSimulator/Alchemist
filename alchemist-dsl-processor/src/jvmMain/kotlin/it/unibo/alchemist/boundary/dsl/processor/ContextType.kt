/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor

/**
 * Type of context available for parameter injection.
 */
enum class ContextType {
    /** Simulation context (only incarnation or only environment). */
    SIMULATION_CONTEXT,

    /** Exporter context (one level below SimulationContext, manually settable). */
    EXPORTER_CONTEXT,

    /** Global programs context (one level below SimulationContext, manually settable). */
    GLOBAL_PROGRAMS_CONTEXT,

    /** Output monitors context (one level below SimulationContext, manually settable). */
    OUTPUT_MONITORS_CONTEXT,

    /** Terminators context (one level below SimulationContext, manually settable). */
    TERMINATORS_CONTEXT,

    /** Deployments context (generator). */
    DEPLOYMENTS_CONTEXT,

    /** Deployment context (singular, one level below DeploymentsContext, includes filter). */
    DEPLOYMENT_CONTEXT,

    /** Program context (includes node, reaction, and time distribution). */
    PROGRAM_CONTEXT,

    /** Property context (includes node, same depth as program context). */
    PROPERTY_CONTEXT,
}
