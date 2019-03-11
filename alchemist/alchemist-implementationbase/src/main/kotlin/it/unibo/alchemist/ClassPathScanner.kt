/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import io.github.classgraph.ClassGraph
import java.io.InputStream
import java.net.URL
import java.util.regex.Pattern

object ClassPathScanner {

    private fun classGraphForPackage(inPackage: String?): ClassGraph = ClassGraph().also {
        if (inPackage != null) {
            it.whitelistPackages(inPackage)
        }
    }

    /**
     * This function loads all subtypes of the provided Java class that can be discovered on the current classpath.
     *
     * This function cannot use `reified` and `inline` (as it should have) due to Java being unaware of the required
     * transformation to use them.
     */
    @JvmStatic
    @JvmOverloads
    @Suppress("UNCHECKED_CAST")
    fun <T> subTypesOf(superClass: Class<T>, inPackage: String? = null): List<Class<out T>> = classGraphForPackage(inPackage)
        .enableClassInfo().scan().let { scanResult ->
            if (superClass.isInterface) {
                scanResult.getClassesImplementing(superClass.name)
            } else {
                scanResult.getSubclasses(superClass.name)
            }.filter { it -> !it.isAbstract }
            .loadClasses()
            .map { it -> it as Class<out T> }
        }

    /**
     * This function returns a list of all the resources in a certain (optional) package matching a regular expression.
     *
     * This function cannot use `reified` and `inline` (as it should have) due to Java being unaware of the required
     * transformation to use them.
     */
    @JvmStatic
    @JvmOverloads
    fun resourcesMatching(regex: String, inPackage: String? = null): List<URL> = classGraphForPackage(inPackage)
        .scan().getResourcesMatchingPattern(Pattern.compile(regex))
        .urLs
        .also { it.forEach { println(it) } }

    @JvmStatic
    @JvmOverloads
    fun resourcesMatchingAsStream(regex: String, inPackage: String? = null): List<InputStream> =
        resourcesMatching(regex, inPackage).map { it.openStream() }
}