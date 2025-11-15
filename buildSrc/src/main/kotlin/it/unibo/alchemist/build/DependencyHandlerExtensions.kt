/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.assign
import org.jetbrains.dokka.gradle.engine.parameters.DokkaSourceSetSpec

fun DependencyHandler.apiAndDoc(dependencyNotation: Project): Dependency? {
    val fromDokka = add("dokka", dependencyNotation)
    return add("api", dependencyNotation) ?: fromDokka
}

context(project: Project)
fun DokkaSourceSetSpec.registerExternal(target: ExternalDependency) {
    val externalDependency = project.fetchExternalLinkSource(target)
    when {
        externalDependency != null -> {
            project.logger.debug("Registering {} for Dokka in {}", externalDependency, project.name)
            externalDocumentationLinks.register(externalDependency.descriptor) {
                url = externalDependency.documentationUrl
                val reference = externalDependency.packageListUrl
                packageListUrl = when {
                    reference.isAbsolute -> reference
                    else -> project.rootProject.uri(reference.toString())
                }
            }
        }
        !target.isJvm && !target.isJs -> registerExternal(target.forJvm())
        target.isJvm -> registerExternal(target.forJs())
        else -> project.logger.debug("Dokka won't link '{}' in project '{}'", target, project.name)
    }
}
