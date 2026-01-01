/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl

import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.dsl.model.SimulationContext
import it.unibo.alchemist.boundary.dsl.model.SimulationContextImpl
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import java.util.concurrent.Semaphore

/**
 * Abstract base class for single-use DSL loaders.
 *
 * @param ctx The simulation context.
 */
abstract class DSLLoader(private val ctx: SimulationContext<*, *>) : Loader {

    protected abstract fun <T, P : Position<P>> envFactory(): Environment<T, P>

    override fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P> =
        SingleUseLoader(ctx).load(values)
    private inner class SingleUseLoader(private val ctx: SimulationContext<*, *>) {
        private val mutex = Semaphore(1)
        private var consumed = false

        @Suppress("UNCHECKED_CAST")
        fun <T, P : Position<P>> load(values: Map<String, *>): Simulation<T, P> {
            try {
                mutex.acquireUninterruptibly()
                check(!consumed) { "This loader has already been consumed! This is a bug in Alchemist" }
                consumed = true
            } finally {
                mutex.release()
            }
            val typedCtx = ctx as SimulationContextImpl<T, P>
            val envInstance = envFactory<T, P>()
            val unknownVariableNames = values.keys - this@DSLLoader.variables.keys
            require(unknownVariableNames.isEmpty()) {
                "Unknown variables provided: $unknownVariableNames." +
                    " Valid names: ${this@DSLLoader.variables.keys}. Provided: ${values.keys}"
            }
            // VARIABLE REIFICATION
            ctx.variablesContext.addReferences(
                ctx.variablesContext.dependentVariables.map { (k, v) ->
                    k to v()
                }.toMap(),
            )
            val simulationIstance = typedCtx.build(envInstance, values)
            val environment = simulationIstance.environment
            val engine = Engine(environment)
            // MONITORS
            simulationIstance.monitors.forEach { monitor ->
                engine.addOutputMonitor(monitor)
            }
            // EXPORTERS
            val exporters = simulationIstance.exporters.map {
                it.type.apply {
                    it.type?.bindDataExtractors(it.extractors)
                }
            } as List<Exporter<T, P>>
            exporters.forEach { it.bindVariables(ctx.variablesContext.references.get()) }
            if (exporters.isNotEmpty()) {
                engine.addOutputMonitor(GlobalExporter(exporters))
            }
            return engine
        }
    }
}
