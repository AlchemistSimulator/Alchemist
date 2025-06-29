/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.tools.ant.taskdefs.condition.Os
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.VerificationTask
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Collector of imperative code.
 */
object Util {
    val isInCI get() = System.getenv("CI") == true.toString()
    val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
    val isMac = Os.isFamily(Os.FAMILY_MAC)
    val isUnix = Os.isFamily(Os.FAMILY_UNIX)

    fun commandExists(command: String) =
        System
            .getenv("PATH")
            .split(File.pathSeparatorChar)
            .any { path -> File(path, command).run { exists() && canExecute() } }

    val Project.currentCommitHash get(): String? =
        kotlin
            .runCatching {
                Git
                    .open(rootProject.projectDir)
                    .repository
                    .resolve("HEAD")
                    ?.name
            }.getOrNull()

    private val javadocIOcacheFile = File("javadoc-io.json")
    private val gson = Gson().newBuilder().setPrettyPrinting().create()
    private val mapType = object : TypeToken<MutableMap<String, Pair<URI, URI?>>>() { }.type

    private val javadocIO: MutableMap<String, Pair<URI, URI?>> =
        javadocIOcacheFile
            .takeIf(File::exists)
            ?.let { gson.fromJson(it.readText(), mapType) }
            ?: mutableMapOf()

    /**
     * If available, finds the URL of the documentation on javadoc.io for [dependency].
     *
     * @return a [Pair] with the URL as a first element, and the packageList URL as second element.
     */
    fun Project.fetchJavadocIOForDependency(dependency: Dependency): Pair<URI, URI>? =
        dependency
            .takeIf { it is ExternalDependency }
            ?.run {
                synchronized(javadocIO) {
                    val size = javadocIO.size
                    val descriptor = "$group/$name/$version"
                    val javadocIOURLs =
                        javadocIO.getOrPut(descriptor) {
                            logger.lifecycle(
                                "Checking javadoc.io for unknown dependency {}:{}:{}",
                                group,
                                name,
                                version,
                            )
                            val urlString = "https://javadoc.io/doc/$descriptor"
                            val packageList =
                                listOf("package-list", "element-list")
                                    .map { URI("$urlString/$it") }
                                    .firstOrNull { runCatching { it.toURL().openStream() }.isSuccess }
                            if (packageList == null) {
                                logger.lifecycle(
                                    "javadoc.io has docs for {}:{}:{}! > {}",
                                    group,
                                    name,
                                    version,
                                    urlString,
                                )
                            }
                            URI(urlString) to packageList
                        }
                    if (javadocIO.size != size) {
                        logger.lifecycle("Caching javadoc.io information for {} at {}", descriptor, javadocIOURLs.first)
                        javadocIOcacheFile.writeText(gson.toJson(javadocIO.toSortedMap()))
                    }
                    javadocIOURLs.second?.let { javadocIOURLs.first to it }
                }
            }

    /**
     * Verifies that the generated shadow jar displays the help, and that SLF4J is not falling back to NOP.
     */
    fun Project.testShadowJar(
        javaExecutable: Provider<String>,
        jarFile: Provider<RegularFile>,
    ) = tasks.register<Exec>(
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

    /**
     * Directly accesses the plugin id.
     */
    val Provider<PluginDependency>.id: String get() = get().pluginId

    /**
     * @return all the tasks performing static verification on the ain the project.
     */
    val TaskContainer.allVerificationTasks get(): TaskCollection<SourceTask> {
        return this.withType<SourceTask>().matching { it is VerificationTask }
    }

    /**
     * Configure a Dev Server for Web targets.
     */
    fun KotlinJsTargetDsl.devServer() {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = static ?: mutableListOf()
                }
            }
        }
    }

    /**
     *  Common configuration for Web targets.
     */
    fun KotlinJsTargetDsl.webCommonConfiguration() {
        outputModuleName.set(project.name)
        browser {
            commonWebpackConfig {
                outputFileName = "${project.name}.js"
            }
        }
        binaries.executable()
    }

    /**
     * Excludes generated files from [PatternFilterable].
     * Mainly used to exclude generated files from static analysis.
     */
    fun PatternFilterable.excludeGenerated() {
        exclude { "generated" in it.file.absolutePath }
    }
}

val Project.catalog get() =
    object : ReadOnlyProperty<Any?, Provider<MinimalExternalModuleDependency>> {
        override operator fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ) = extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")
            .findLibrary(property.name.replace(Regex("[A-Z]")) { "-${it.value.lowercase()}" })
            .get()
    }
