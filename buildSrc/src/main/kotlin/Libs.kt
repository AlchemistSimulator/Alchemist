/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import org.gradle.api.Project
import kotlin.String

/**
 * Statically defined libraries used by the project.
 */
@Suppress("UndocumentedPublicProperty")
object Libs {
    const val javalib_java7: String = "org.danilopianini:javalib-java7:_"
    const val jirf: String = "org.danilopianini:jirf:_"
    const val jool: String = "org.jooq:jool:_"
    const val listset: String = "org.danilopianini:listset:_"
    const val mapsforge_map_awt: String = "org.mapsforge:mapsforge-map-awt:_"
    const val slf4j_api: String = "org.slf4j:slf4j-api:_"
    const val snakeyaml: String = "org.yaml:snakeyaml:_"
    const val svgsalamander: String = "guru.nidi.com.kitfox:svgSalamander:1.1.2"
    const val thread_inheritable_resource_loader: String = "org.danilopianini:thread-inheritable-resource-loader:_"

    /**
     * Returns a reference to an alchemist sub-project [module].
     */
    fun Project.alchemist(module: String) = project(":alchemist-$module")

    /**
     * Returns a reference to an alchemist sub-project incarnation [module].
     */
    fun Project.incarnation(module: String) = alchemist("incarnation-$module")

    private fun modularizedLibrary(base: String, module: String = "", separator: String = "-") = when {
        module.isEmpty() -> base
        else -> base + separator + module
    } + ":_"

    /**
     * Returns the identifier of the desired GraphStream [module].
     */
    fun graphStream(module: String = "") = modularizedLibrary("org.graphstream:gs", module)

    /**
     * Returns the identifier of the desired JGraphT [module].
     */
    fun jgrapht(module: String = "") = modularizedLibrary("org.jgrapht:jgrapht", module)

    /**
     * Returns the identifier of the desired Protelis [module].
     */
    fun protelis(module: String = "") = modularizedLibrary("org.protelis:protelis", module)

    /**
     * Returns the identifier of the desired Scala [module].
     */
    fun scalaModule(module: String = "") = modularizedLibrary("org.scala-lang:scala", module)
}
