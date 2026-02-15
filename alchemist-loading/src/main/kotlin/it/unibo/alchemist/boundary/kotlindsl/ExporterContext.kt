/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Position

/**
 * DSL scope for configuring exporters by collecting [Extractor] instances.
 *
 * Implementations of this context are expected to translate unary-minus applications on extractors into
 * registrations/collection operations (e.g., adding the extractor to a list that will later be bound to
 * an exporter).
 *
 * The type parameters are carried to keep the DSL aligned with the simulation types, even though this
 * context only deals with extractors at the boundary layer.
 *
 * @param T the concentration type used by the simulation.
 * @param P the position type used by the environment.
 */
interface ExporterContext<T, P : Position<P>> {

    /**
     * Registers this [Extractor] in the current exporter configuration.
     *
     * This operator is intended to provide concise DSL syntax, where applying unary minus to an extractor
     * means "include this extractor among those used by the exporter".
     */
    operator fun Extractor<*>.unaryMinus()
}
