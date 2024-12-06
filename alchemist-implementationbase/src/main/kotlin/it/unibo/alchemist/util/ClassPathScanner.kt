/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.base.Objects
import io.github.classgraph.ClassGraph
import java.io.InputStream
import java.lang.reflect.Modifier
import java.net.URL
import java.util.regex.Pattern

/**
 * An utility class providing support for loading arbitrary subclasses available in the classpath.
 */
object ClassPathScanner {
    private val loader =
        Caffeine.newBuilder().build<ScanData, List<Class<*>>> { scanData ->
            classGraphForPackages(*scanData.inPackages)
                .enableClassInfo()
                .scan()
                .let { scanResult ->
                    if (scanData.superClass.isInterface) {
                        scanResult.getClassesImplementing(scanData.superClass.name)
                    } else {
                        scanResult.getSubclasses(scanData.superClass.name)
                    }
                }.filter { !it.isAbstract }
                .loadClasses()
        }

    private fun classGraphForPackages(vararg inPackage: String): ClassGraph =
        ClassGraph()
            .apply {
                // WHITELIST package
                acceptPackages(*inPackage)
                // BLACKLIST package
                rejectPackages("org.gradle")
            }

    /**
     * This function loads all subtypes of the provided Java class that can be discovered on the current classpath.
     *
     * This function cannot use `reified` and `inline` (as it should have) due to Java being unaware of the required
     * transformation to use them.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> subTypesOf(
        superClass: Class<T>,
        vararg inPackage: String,
    ): List<Class<out T>> =
        when {
            Modifier.isFinal(superClass.modifiers) -> listOf(superClass)
            else -> loader[ScanData(superClass, inPackage)] as List<Class<out T>>
        }

    /**
     * This function loads all subtypes of the provided Java class that can be discovered on the current classpath.
     */
    inline fun <reified T> subTypesOf(vararg inPackage: String): List<Class<out T>> =
        subTypesOf(T::class.java, *inPackage)

    /**
     * This function returns a list of all the resources in a certain (optional) package matching a regular expression.
     *
     * This function cannot use `reified` and `inline` (as it should have) due to Java being unaware of the required
     * transformation to use them.
     */
    @JvmStatic
    fun resourcesMatching(
        regex: String,
        vararg inPackage: String,
    ): List<URL> =
        classGraphForPackages(*inPackage)
            .scan()
            .getResourcesMatchingPattern(Pattern.compile(regex))
            .urLs

    /**
     * This function returns a list of all the resources in a certain (optional) package matching a regular expression.
     */
    @JvmStatic
    fun resourcesMatchingAsStream(
        regex: String,
        vararg inPackage: String,
    ): List<InputStream> = resourcesMatching(regex, *inPackage).map { it.openStream() }

    private data class ScanData(
        val superClass: Class<*>,
        val inPackages: Array<out String>,
    ) {
        val hashCode = Objects.hashCode(superClass, *inPackages)

        override fun equals(other: Any?) =
            other === this ||
                other is ScanData &&
                superClass == other.superClass &&
                inPackages.contentEquals(other.inPackages)

        override fun hashCode(): Int = hashCode

        override fun toString(): String = "ScanData(${superClass.simpleName}, ${inPackages.asList()})"
    }
}
