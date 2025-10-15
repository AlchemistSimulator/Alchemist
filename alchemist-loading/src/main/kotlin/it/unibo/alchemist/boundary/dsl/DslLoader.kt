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
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.dsl.model.SimulationContext
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

abstract class DslLoader(private val ctx: SimulationContext<*, *>) : Loader {
    @Suppress("UNCHECKED_CAST")
    override fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P> {
        val environment = ctx.environment as Environment<T, P>
        println("Creating engine")
        val engine = Engine(environment)
        val unknownVariableNames = values.keys - variables.keys
        require(unknownVariableNames.isEmpty()) {
            "Unknown variables provided: $unknownVariableNames." +
                " Valid names: ${variables.keys}. Provided: ${values.keys}"
        }
        // VARIABLE REIFICATION
        val variableValues = variables.mapValues { (name, previous) ->
            if (values.containsKey(name)) values[name] else previous.default
        }
        // MONITORS
        ctx.monitors.forEach { monitor ->
            engine.addOutputMonitor(monitor as OutputMonitor<T, P>)
        }
        // EXPORTERS
        val exporters = ctx.exporters.map {
            it.type.apply {
                it.type?.bindDataExtractors(it.extractors)
            }
        } as List<Exporter<T, P>>

        exporters.forEach { it.bindVariables(variableValues) }

        if (exporters.isNotEmpty()) {
            engine.addOutputMonitor(GlobalExporter(exporters))
        }

        return engine
    }
}
