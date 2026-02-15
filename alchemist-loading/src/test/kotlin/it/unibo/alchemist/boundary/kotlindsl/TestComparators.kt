/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.test.equalsForSteps

/**
 * Main test comparison class that handles static and runtime comparisons
 *
 * This class provides an interface for comparing DSL and YAML loaders
 * with the option to include runtime behavior testing.
 */
object TestComparators {

    /**
     * Compares a DSL loader with a YAML loader.
     *
     * @param other The DSL loader to compare.
     * @param yamlResource The YAML resource path to compare against.
     * @param steps The number of steps for runtime comparison (only used if includeRuntime is true).
     *   Exactly one of steps, targetTime, or stableForSteps must be provided.
     */
    fun <T : Any, P : Position<P>> Loader.shouldEqual(yamlResource: String, steps: Long = 0L) {
        val yamlLoader = LoaderFactory.loadYaml(yamlResource)
        // Always perform static comparison
        getDefault<T, P>().equalsForSteps(yamlLoader.getDefault<T, P>(), steps)
    }
}
