/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.Loader
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Factory for creating and loading DSL and YAML loaders for testing.
 */
object LoaderFactory {

    /**
     * Loads a DSL loader from a resource path.
     */
    fun loadDsl(dslCode: String): Loader = throw NotImplementedError("Not implemented yet $dslCode")

    /**
     * Loads a YAML loader from a resource path.
     */
    fun loadYaml(yamlResource: String): Loader = LoadAlchemist.from(ResourceLoader.getResource(yamlResource)!!)

    /**
     * Loads both DSL and YAML loaders for comparison.
     */
    fun loadBoth(dslCode: String, yamlResource: String): Pair<Loader, Loader> =
        Pair(loadDsl(dslCode), loadYaml(yamlResource))
}
