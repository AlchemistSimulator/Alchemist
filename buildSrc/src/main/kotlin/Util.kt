import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.artifacts.ExternalDependency
import java.io.File
import java.net.URL

/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * Collector of imperative code.
 */
object Util {

    private val javadocIOcacheFile = File("javadoc-io.json")
    private val gson = Gson().newBuilder().setPrettyPrinting().create()
    private val mapType = object : TypeToken<MutableMap<String, Pair<URL, URL?>>>() { }.type

    @Suppress("UNCHECKED_CAST")
    private val javadocIO: MutableMap<String, Pair<URL, URL?>> = javadocIOcacheFile
        .takeIf(File::exists)
        ?.let { gson.fromJson(it.readText(), mapType) }
        ?: mutableMapOf()

    /**
     * If available, finds the URL of the documentation on javadoc.io for [dependency].

     * @return a [Pair] with the URL as a first element, and the packageList URL as second element.
     */
    fun Project.fetchJavadocIOForDependency(dependency: Dependency): Pair<URL, URL>? = dependency
        .takeIf { it is ExternalDependency }
        ?.run {
            synchronized(javadocIO) {
                val size = javadocIO.size
                val descriptor = "$group/$name/$version"
                val javadocIOURLs = javadocIO.getOrPut(descriptor) {
                    logger.lifecycle("Checking javadoc.io for unknown dependency {}:{}:{}", group, name, version)
                    val urlString = "https://javadoc.io/doc/$descriptor"
                    val packageList = listOf("package-list", "element-list")
                        .map { URL("$urlString/$it") }
                        .firstOrNull { runCatching { it.openStream() }.isSuccess }
                    if (packageList == null) {
                        logger.lifecycle("javadoc.io has docs for {}:{}:{}! > {}", group, name, version, urlString)
                    }
                    URL(urlString) to packageList
                }
                if (javadocIO.size != size) {
                    logger.lifecycle("Caching javadoc.io information for {} at {}", descriptor, javadocIOURLs.first)
                    javadocIOcacheFile.writeText(gson.toJson(javadocIO.toSortedMap()))
                }
                javadocIOURLs.second?.let { javadocIOURLs.first to it }
            }
        }
}
