/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import it.unibo.alchemist.build.ExternalDependency
import it.unibo.alchemist.build.currentCommitHash
import it.unibo.alchemist.build.registerExternal
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask

/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
plugins {
    id("org.jetbrains.dokka")
}

val minJavaVersion: String by properties

val fetchEngine: Executor = Executors.newCachedThreadPool()

dokka {
    dokkaPublications.configureEach {
        failOnWarning.set(true)
    }
    dokkaSourceSets.configureEach {
        enableKotlinStdLibDocumentationLink.set(true)
        enableJdkDocumentationLink.set(true)
        jdkVersion.set(minJavaVersion.toInt())
        skipDeprecated.set(false)
        skipEmptyPackages.set(true)
        /*
         * Source links to GitHub
         */
        listOf("kotlin", "java")
            .flatMap { listOf("main/$it", "commonMain/$it", "jsMain/$it", "jvmMain/$it") }
            .map { "src/$it" }
            .associateWith { File(projectDir, it) }
            .filterValues { it.exists() }
            .forEach { (path, file) ->
                sourceLink {
                    localDirectory.set(file)
                    val project = if (project == rootProject) "" else project.name
                    val url = "https://github.com/AlchemistSimulator/Alchemist/${
                        currentCommitHash?.let { "tree/$it" } ?: "blob/master"
                    }/$project/$path"
                    remoteUrl.set(uri(url))
                    remoteLineSuffix.set("#L")
                }
            }
        /*
         * Links for Alchemist modules
         */
        rootProject.subprojects.forEach {
            registerExternal(
                ExternalDependency(it.group.toString(), it.name, it.version.toString().substringBefore('-'))
            )
        }
        /*
         * Links for external dependencies
         */
        project.versionCatalogs.forEach { versionCatalog ->
            versionCatalog.libraryAliases.forEach { alias ->
                val lib = versionCatalog.findLibrary(alias).get().get()
                registerExternal(ExternalDependency(lib.group, lib.name, lib.version))
            }
        }
        pluginsConfiguration.html {
            customAssets.from(rootProject.file("site/static/images/logo.svg"))
            customStyleSheets.from(rootProject.file("site/logo-styles.css"))
            footerMessage.set("(c) Danilo Pianini and contributors listed in the Alchemist build files")
            homepageLink = "https://alchemistsimulator.github.io/"
        }
    }
}

tasks.withType<DokkaBaseTask>().configureEach {
    timeout.set(Duration.ofMinutes(5))
}
