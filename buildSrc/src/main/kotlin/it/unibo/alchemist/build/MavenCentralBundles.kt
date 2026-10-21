/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import org.gradle.api.GradleException

/*
 * Every Alchemist module depends on the Kotlin standard library,
 * so any jar embedding it bundles its dependencies.
 */
private const val BUNDLED_DEPENDENCY_MARKER = "kotlin/Unit.class"

/**
 * Fails if the Maven Central Portal [bundle] contains a fat jar,
 * recognized either by the Shadow `all` classifier or by embedding the Kotlin standard library.
 */
fun checkNoFatJarIn(bundle: File) {
    val fatJars = ZipFile(bundle).use { zip ->
        zip.entries().asSequence()
            .filter { it.name.endsWith(".jar") }
            .filter { entry ->
                entry.name.endsWith("-all.jar") ||
                    zip.getInputStream(entry).use { it.containsEntry(BUNDLED_DEPENDENCY_MARKER) }
            }
            .map { it.name }
            .toList()
    }
    if (fatJars.isNotEmpty()) {
        throw GradleException("Fat jars must not be published to Maven Central, but ${bundle.name} contains $fatJars")
    }
}

private fun InputStream.containsEntry(name: String): Boolean = ZipInputStream(this).let { jar ->
    generateSequence { jar.nextEntry }.any { it.name == name }
}
