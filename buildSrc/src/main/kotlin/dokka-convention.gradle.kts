import Util.currentCommitHash
import Util.fetchJavadocIOForDependency
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask
import java.time.Duration

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

dokka {
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
         * Javadoc.io links for external dependencies
         */
        val configured = mutableSetOf<ExternalDependency>()
        configurations.configureEach {
            val newDependencies = dependencies.withType<ExternalDependency>() - configured
            configured += newDependencies
            newDependencies.forEach { dependency ->
                val javadocIOURLs = fetchJavadocIOForDependency(dependency)
                if (javadocIOURLs != null) {
                    val (javadoc, packageList) = javadocIOURLs
                    externalDocumentationLinks.register(dependency.name) {
                        url.set(javadoc)
                        packageListUrl.set(packageList)
                    }
                }
            }
        }
        pluginsConfiguration.html {
            customAssets.from(rootProject.file("site/static/images/logo.svg"))
            customStyleSheets.from(rootProject.file("site/logo-styles.css"))
            footerMessage.set("(c) Danilo Pianini and contributors listed in the Alchemist build files")
        }
    }
}

tasks.withType<DokkaBaseTask>().configureEach {
    timeout.set(Duration.ofMinutes(5))
}
