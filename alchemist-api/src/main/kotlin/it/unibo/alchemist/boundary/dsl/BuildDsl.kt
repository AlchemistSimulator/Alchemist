/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl

/**
 * Annotation used to mark classes that should have DSL builder functions generated.
 * When applied to a class, the DSL processor will generate a builder function
 * that can be used in Alchemist DSL scripts to create instances of the annotated class.
 *
 * @param functionName Custom name for the generated DSL function. If empty, defaults to
 *   the lowercase version of the class name with the first character lowercased.
 * @param scope Manual override for the context type. Valid values (case-insensitive):
 *   - "SIMULATION" or "SIMULATION_CONTEXT" → SimulationContext
 *   - "EXPORTER" or "EXPORTER_CONTEXT" → ExporterContext
 *   - "GLOBAL_PROGRAMS" or "GLOBAL_PROGRAMS_CONTEXT" → GlobalProgramsContext
 *   - "OUTPUT_MONITORS" or "OUTPUT_MONITORS_CONTEXT" → OutputMonitorsContext
 *   - "TERMINATORS" or "TERMINATORS_CONTEXT" → TerminatorsContext
 *   - "DEPLOYMENT" or "DEPLOYMENTS_CONTEXT" → DeploymentsContext
 *   - "DEPLOYMENT_CONTEXT" → DeploymentContext (singular)
 *   - "PROGRAM" or "PROGRAM_CONTEXT" → ProgramContext
 *   - "PROPERTY" or "PROPERTY_CONTEXT" → PropertyContext
 *   If empty, the context type is automatically determined based on the constructor parameters.
 *   This allows manual context passing to override the default behavior.
 * @param injectEnvironment Whether to inject an Environment parameter into the generated builder function.
 * @param injectGenerator Whether to inject a Generator parameter into the generated builder function.
 * @param injectNode Whether to inject a Node parameter into the generated builder function.
 * @param injectReaction Whether to inject a Reaction parameter into the generated builder function.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BuildDsl(
    val functionName: String = "",
    val scope: String = "",
    val injectEnvironment: Boolean = true,
    val injectGenerator: Boolean = true,
    val injectNode: Boolean = true,
    val injectReaction: Boolean = true,
)
