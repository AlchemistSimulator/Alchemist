/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.properties.ReadOnlyProperty
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

private val externalKnown: File get() = dokkaCacheFolder.resolve("javadoc-cache.json")
private val gson = Gson().newBuilder().setPrettyPrinting().create()
private val mapType = object : TypeToken<ConcurrentHashMap<String, ExternalDocumentationLink>>() { }.type
private val externalDocsCache: ConcurrentMap<String, ExternalDocumentationLink> = run {
    val externalDocCacheFile: File = dokkaCacheFolder.resolve("javadoc-cache.json")
    val delegate = externalDocCacheFile.loadOrCreate<ConcurrentHashMap<String, ExternalDocumentationLink>>(::ConcurrentHashMap)
    object : ConcurrentMap<String, ExternalDocumentationLink> by delegate {
        override fun put(key: String, value: ExternalDocumentationLink): ExternalDocumentationLink? =
            synchronized(externalDocCacheFile) {
                delegate.put(key, value)?.also { _ ->
                    externalDocCacheFile.writeText(gson.toJson(delegate.toSortedMap()))
                }
            }
    }
}

private val fetchFailuresCache: MutableSet<ExternalDependency> = run {
    val fetchFailuresFile: File = dokkaCacheFolder.resolve("no-javadoc.json")
    val delegate = fetchFailuresFile.loadOrCreate<ConcurrentSkipListSet<ExternalDependency>>(::ConcurrentSkipListSet)
    object : MutableSet<ExternalDependency> by delegate {
        override fun add(element: ExternalDependency): Boolean = synchronized(fetchFailuresFile) {
            delegate.add(element).also { anElementHasBeenAdded ->
                if (anElementHasBeenAdded) fetchFailuresFile.writeText(gson.toJson(delegate))
            }
        }
    }
}

private inline fun <reified T : Any> File.loadOrCreate(default: () -> T): T =
    takeIf(File::exists)?.let { gson.fromJson(readText(), object : TypeToken<T>() { }) }
        ?: default()

val dokkaCacheFolder get() = File("dokka-cache").apply { mkdirs() }

val Project.catalog get() = ReadOnlyProperty<Any?, Provider<MinimalExternalModuleDependency>> { thisRef, property ->
    extensions.getByType<VersionCatalogsExtension>().named("libs")
        .findLibrary(property.name.replace(Regex("[A-Z]")) { "-${it.value.lowercase()}" }).get()
}

val Project.currentCommitHash get(): String? = runCatching {
    Git.open(rootProject.projectDir).repository.resolve("HEAD") ?.name
}.getOrNull()

inline fun <reified T1: Task, reified T2: Task> Project.bindTasks() {
    tasks.withType<T1>().configureEach { dependsOn(tasks.withType<T2>()) }
}

/**
 * If available, finds the URL of the documentation on javadoc.io for [dependency].
 *
 * @return a [Pair] with the URL as a first element, and the packageList URL as second element.
 */
fun Project.fetchExternalLinkSource(dependency: ExternalDependency): ExternalDocumentationLink? {
    logger.debug("Fetching external documentation for {}", dependency)
    return when {
        dependency in fetchFailuresCache -> {
            logger.debug("External documentation for {} cannot be retrieved", dependency)
            null
        }
        else -> {
            val fetched = externalDocsCache[dependency.toString()]?.also {
                logger.debug("External documentation for {} found in cache", dependency)
            } ?: dependency.fetchOrNull(this)?.also { newDependencyToCache ->
                    externalDocsCache[dependency.toString()] = newDependencyToCache
                }
            if (fetched == null) {
                fetchFailuresCache += dependency
                logger.debug("Could not find documentation for {}", dependency)
            }
            fetched
        }
    }
}

/**
 * Verifies that the generated shadow jar displays the help, and that SLF4J is not falling back to NOP.
 */
fun Project.testShadowJar(javaExecutable: Provider<String>, jarFile: Provider<RegularFile>) = tasks.register<Exec>(
    "test${
        jarFile.get().asFile.nameWithoutExtension
            .removeSuffix("-all")
            .removePrefix("alchemist-")
            .replaceFirstChar { it.titlecaseChar() }
            .replace(Regex("-([a-z])")) { it.groupValues[1].uppercase() }
            .replace("-", "")
    }ShadowJarOutput",
) {
    group = "Verification"
    description = "Verifies the terminal output correctness when printing the help via ${jarFile.get().asFile.name}"
    val interceptOutput = ByteArrayOutputStream()
    val interceptError = ByteArrayOutputStream()
    standardOutput = interceptOutput
    errorOutput = interceptError
    isIgnoreExitValue = true
    doFirst {
        commandLine(javaExecutable.get(), "-jar", jarFile.get().asFile.absolutePath, "--help")
    }
    doLast {
        val exit = executionResult.get().exitValue
        require(exit == 0) {
            val outputs = listOf(interceptOutput, interceptError).map { String(it.toByteArray(), Charsets.UTF_8) }
            outputs.forEach { text ->
                for (illegalKeyword in listOf("SLF4J", "NOP")) {
                    require(illegalKeyword !in text) {
                        """
                            |$illegalKeyword found while printing the help. Complete output:
                            |$text
                            """.trimMargin()
                    }
                }
            }
            """
                |Process '${commandLine.joinToString(" ")}' exited with $exit
                |Output:
                |${outputs[0]}
                |Error:
                |${outputs[0]}
            """.trimMargin()
        }
    }
}
