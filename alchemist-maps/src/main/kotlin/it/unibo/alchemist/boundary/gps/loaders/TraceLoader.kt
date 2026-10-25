/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders

import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterators
import it.unibo.alchemist.boundary.FileFormatException
import it.unibo.alchemist.boundary.gps.GPSFileLoader
import it.unibo.alchemist.boundary.gps.GPSTimeAlignment
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.util.ClassPathScanner
import java.io.IOException
import java.util.Locale
import java.util.Optional
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.FactoryBuilder
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Loads [GPSTrace]s from the file or directory at [path], aligns their time using [normalizer],
 * and makes them iterable, cyclically if [cyclic] is true.
 */
class TraceLoader @JvmOverloads constructor(
    path: String,
    private val cyclic: Boolean = false,
    normalizer: GPSTimeAlignment,
) : Iterable<GPSTrace> {
    private val traces: ImmutableList<GPSTrace> = normalizer.alignTime(loadTraces(path))

    /**
     * Builds a new loader reading the traces at [path] and aligning their time with a [GPSTimeAlignment]
     * created by class name ([timeNormalizerClass]) with the given [normalizerArgs].
     * If [cyclic] is true, the traces are iterated cyclically.
     */
    @JvmOverloads
    constructor(
        path: String,
        cyclic: Boolean = false,
        timeNormalizerClass: String,
        vararg normalizerArgs: Any?,
    ) : this(path, cyclic, makeNormalizer(timeNormalizerClass, *normalizerArgs))

    override fun iterator(): Iterator<GPSTrace> = if (cyclic) Iterators.cycle(traces) else traces.iterator()

    /**
     * Returns the number of loaded traces, or `null` if the traces are iterated cyclically.
     */
    val size: Int? = traces.size.takeUnless { cyclic }

    private companion object {
        private val LOADERS: Map<String, GPSFileLoader> = ClassPathScanner
            .subTypesOf<GPSFileLoader>("it.unibo.alchemist")
            .asSequence()
            .map { it.getConstructor().newInstance() }
            .flatMap { loader -> loader.supportedExtensions.map { it.lowercase(Locale.US) to loader } }
            .toMap()

        private val FACTORY: Factory = FactoryBuilder()
            .withWideningConversions()
            .withNarrowingConversions()
            .withAutoBoxing<Any>()
            .build()

        private val AVAILABLE_GPS_TIME_ALIGNMENT: List<Class<out GPSTimeAlignment>> =
            ClassPathScanner.subTypesOf<GPSTimeAlignment>()

        private fun loadTraces(path: String): List<GPSTrace> {
            val isDirectory = runOnPaths(path) { paths -> paths.all { ResourceLoader.getResource(it) != null } }
            return when {
                isDirectory -> runOnPaths(path) { it.toList() }.flatMap { loadTraces(it) }
                else -> {
                    val extension = path.substringAfterLast('.').lowercase(Locale.US)
                    val fileLoader = requireNotNull(LOADERS[extension]) {
                        "no loader defined for file with extension: $extension (file: $path)"
                    }
                    try {
                        fileLoader.readTrace(ResourceLoader.getResource(path))
                    } catch (e: FileFormatException) {
                        throw IllegalStateException(
                            "Loader ${fileLoader::class.simpleName} can't load file $path, " +
                                "please make sure it is a valid $extension file",
                            e,
                        )
                    }
                }
            }
        }

        private fun makeNormalizer(className: String, vararg args: Any?): GPSTimeAlignment {
            val nameOf: (Class<*>) -> String = if ('.' in className) ({ it.name }) else ({ it.simpleName })
            val targetClass = requireNotNull(AVAILABLE_GPS_TIME_ALIGNMENT.find { nameOf(it) == className }) {
                "Normalizer with class name $className not found. " +
                    "Available GPSTimeAlignment are: ${AVAILABLE_GPS_TIME_ALIGNMENT.map { it.name }}"
            }
            return synchronized(FACTORY) {
                FACTORY.build(targetClass, *args).createdObjectOrThrowException
            }
        }

        /**
         * Runs [operation] on the entries listed by the resource at [path], each prefixed by [path].
         * If the resource is a directory, the entries are its children.
         */
        private fun <R> runOnPaths(path: String, operation: (Sequence<String>) -> R): R {
            val resourceStream = requireNotNull(ResourceLoader.getResourceAsStream(path)) {
                "resource not found: '$path', make sure the file exists in the classpath"
            }
            return try {
                resourceStream.bufferedReader().use { reader -> operation(reader.lineSequence().map { "$path/$it" }) }
            } catch (e: IOException) {
                throw IllegalArgumentException("error reading lines of: $path", e)
            }
        }
    }
}
