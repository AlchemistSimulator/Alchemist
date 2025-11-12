/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import java.io.File
import java.net.URI
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency

data class ExternalDependency(val group: String, val name: String, val version: String?) : Comparable<ExternalDependency> {

    val module get() = name
    val isJvm get() = name.endsWith("-jvm")
    val isJs get() = name.endsWith("-js")

    override fun compareTo(other: ExternalDependency): Int = toString().compareTo(other.toString())

    private fun fetchDocFromKotlinLangDotOrg(): ExternalDocumentationLink? =
        group.takeIf { it.startsWith("org.jetbrains.kotlin") }?.let{ _ ->
            sequenceOf("", "core/").firstNotNullOfOrNull { subPath ->
                val docUrl = "https://kotlinlang.org/api/$subPath$module"
                val packageList = URI("$docUrl/package-list")
                runCatching { packageList.toURL().openStream() }
                    .map { ExternalDocumentationLink(this, docUrl, packageList) }
                    .getOrNull()
            }
        }

    private fun fetchDocFromJavadocIO(project: Project): ExternalDocumentationLink? {
        val javadocIOBaseUrl = "https://javadoc.io"
        val moduleSubUrl = "$group/$module"
        val moduleSubUrlWithVersion = "$moduleSubUrl/$version"
        val javadocIOUrl = URI("$javadocIOBaseUrl/doc/$moduleSubUrlWithVersion/")
        return runCatching { javadocIOUrl.toURL().readText() }
            .recoverCatching {
                project.logger.lifecycle(
                    "Failed to fetch Javadoc.io page for {} at {}: {}. Retrying...",
                    this,
                    javadocIOUrl,
                    it.message
                )
                javadocIOUrl.toURL().readText()
            }.recoverCatching {
                println("Failed to fetch Javadoc.io page for $this at $javadocIOUrl: ${it.message}. Trying with no version...")
                javadocIOUrl.toURL().readText()
            }.map { contents ->
                when {
                    contents.contains("no javadoc is released", ignoreCase = true) -> {
                        project.logger.lifecycle("{} does not release Javadoc/Dokka on Javadoc.io", this)
                        null
                    }
                    else -> {
                        val actualVersion = javadocIOversionMatcher(group, module, contents) ?: version
                        if (version != actualVersion) {
                            project.logger.debug(
                                "Requested version {} for {}, but found version {} on Javadoc.io",
                                version,
                                this,
                                actualVersion
                            )
                        }
                        val localPackageListContainer = dokkaCacheFolder.resolve(group).resolve(module)
                        check(dokkaCacheFolder.exists())
                        val localPackageList: File = localPackageListContainer.resolve("$actualVersion.list")
                        val cachedPackageList: File? = localPackageList.takeIf { it.exists() }
                            ?: sequenceOf("package-list", "element-list", "$module/package-list")
                                .firstNotNullOfOrNull {
                                    val uri = URI("$javadocIOBaseUrl/static/$moduleSubUrl/$actualVersion/$it")
                                    runCatching { uri.toURL().readText() }
                                        .map { packageList ->
                                            project.rootProject.projectDir.resolve(localPackageListContainer).mkdirs()
                                            check(localPackageListContainer.exists() && localPackageListContainer.isDirectory)
                                            localPackageList.writeText(packageList)
                                            localPackageList
                                        }
                                        .getOrNull()
                                }
                        cachedPackageList?.let { packageList ->
                            ExternalDocumentationLink(this, javadocIOUrl, localPackageList.path)
                        }
                    }
                }
            }.getOrNull()
    }

    fun fetchOrNull(project: Project): ExternalDocumentationLink? = fetchDocFromKotlinLangDotOrg() ?: fetchDocFromJavadocIO(project)

    fun forJvm() = when {
        isJvm -> this
        isJs -> ExternalDependency(group, name.removeSuffix("-js") + "-jvm", version)
        else -> ExternalDependency(group, "$name-jvm", version)
    }

    fun forJs() = when {
        isJs -> this
        isJvm -> ExternalDependency(group, name.removeSuffix("-jvm") + "-js", version)
        else -> ExternalDependency(group, "$name-jsz", version)
    }

    override fun toString() = "$group:$name${version?.let { ":$it" } ?: ""}"

    companion object {
        private fun javadocIOversionMatcher(group: String, module: String, content: String) =
            Regex("""<title>\s*$module\s+(\S+)\s+javadoc\s*\($group\)\s*</title>""")
                .find(content)
                ?.destructured?.component1()
    }
}

fun MinimalExternalModuleDependency.toExternalDependency(): ExternalDependency =
    ExternalDependency(group, name, version)

