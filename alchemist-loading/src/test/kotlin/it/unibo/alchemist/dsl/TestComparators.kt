/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.model.Position

/**
 * Main test comparison class that handles static and runtime comparisons
 *
 * This class provides an interface for comparing DSL and YAML loaders
 * with the option to include runtime behavior testing.
 */
object TestComparators {

    /**
     * Compares a DSL loader with a YAML loader
     *
     * @param dslLoader The DSL loader to compare
     * @param yamlResource The YAML resource path to compare against
     * @param includeRuntime Whether to include runtime behavior comparison
     * @param steps The number of steps for runtime comparison (only used if includeRuntime is true)
     */
    fun <T, P : Position<P>> compare(
        dslLoader: () -> Loader,
        yamlResource: String,
        includeRuntime: Boolean = false,
        steps: Long = 1000L,
    ) {
        val yamlLoader = LoaderFactory.loadYaml(yamlResource)

        // Always perform static comparison
        StaticComparisonHelper.compareBasicProperties(dslLoader(), yamlLoader)
        StaticComparisonHelper.compareSimulations<T, P>(dslLoader(), yamlLoader)

        // Optionally perform runtime comparison
        if (includeRuntime) {
            RuntimeComparisonHelper.compareLoaders<T, P>(dslLoader(), yamlLoader, steps)
        }
    }

    /**
     * Compares DSL code with a YAML resource
     *
     * @param dslCode The DSL code resource path
     * @param yamlResource The YAML resource path to compare against
     * @param includeRuntime Whether to include runtime behavior comparison
     * @param steps The number of steps for runtime comparison (only used if includeRuntime is true)
     */
    fun <T, P : Position<P>> compare(
        dslCode: String,
        yamlResource: String,
        includeRuntime: Boolean = false,
        steps: Long = 3000L,
    ) {
        compare<T, P>({
            LoaderFactory.loadDsl(dslCode)
        }, yamlResource, includeRuntime, steps)
    }
}

/**
 * Extension function for easier test writing with static comparison only
 */
fun Loader.shouldEqual(yamlResource: String) {
    @Suppress("UNCHECKED_CAST")
    TestComparators.compare<Any, Nothing>({ this }, yamlResource, includeRuntime = false)
}

/**
 * Extension function for comparing DSL function with YAML resource
 */
fun (() -> Loader).shouldEqual(yamlResource: String, includeRuntime: Boolean = true, steps: Long = 3000L) {
    @Suppress("UNCHECKED_CAST")
    TestComparators.compare<Any, Nothing>(this, yamlResource, includeRuntime, steps)
}
