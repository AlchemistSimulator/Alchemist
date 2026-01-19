/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Libs.incarnation
import com.github.spotbugs.snom.SpotBugsTask
import it.unibo.alchemist.build.id
import it.unibo.alchemist.build.isInCI
import it.unibo.alchemist.build.isMac
import it.unibo.alchemist.build.isWindows
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask

plugins {
    id("kotlin-jvm-convention")
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.hugo)
}

val minJavaVersion: String by properties

allprojects {

    with(rootProject.libs.plugins) {
        apply(plugin = gitSemVer.id)
        apply(plugin = multiJvmTesting.id)
        apply(plugin = publishOnCentral.id)
        apply(plugin = taskTree.id)
    }

    multiJvm {
        jvmVersionForCompilation.set(minJavaVersion.toInt())
        maximumSupportedJvmVersion.set(latestJava)
        if (isInCI && (isWindows || isMac)) {
            /*
             * Reduce time in CI by running on fewer JVMs on slower or more limited instances.
             */
            testByDefaultWith(latestJava)
        }
    }

    repositories {
        google()
        mavenCentral()
    }

    // TEST AND COVERAGE

    tasks.withType<Test>().configureEach {
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
        useJUnitPlatform()
        maxHeapSize = "1g"
        "alchemist.engine".let { property ->
            System.getProperty(property)?.let { systemProperty(property, it) }
        }
    }

    tasks.withType<SpotBugsTask>().configureEach {
        reports {
            create("html") { enabled = true }
        }
    }

    // PUBLISHING

    tasks.withType<Javadoc>().configureEach {
        // Disable Javadoc, use Dokka.
        enabled = false
    }

    /*
     * Work around:
     * Task ':...:dokkaJavadoc' uses this output of task ':...:jar' without declaring an explicit or implicit dependency.
     * This can lead to incorrect results being produced, depending on what order the tasks are executed.
     */
    tasks.withType<AbstractDokkaTask>().configureEach {
        allprojects.forEach { otherProject ->
            dependsOn(otherProject.tasks.withType<org.gradle.jvm.tasks.Jar>().matching { it.name == "jar" })
        }
    }

    if (isInCI) {
        signing {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }

    group = "it.unibo.alchemist"
    val repoSlug = "AlchemistSimulator/Alchemist"
    publishOnCentral {
        projectDescription.set(extra["projectDescription"].toString())
        projectLongName.set(extra["projectLongName"].toString())
        projectUrl.set("https://github.com/$repoSlug")
        licenseName.set("GPL 3.0 with linking exception")
        licenseUrl.set(projectUrl.map { "$it/blob/master/LICENSE.md" })
        scmConnection.set("git:git@github.com:$repoSlug.git")
        repository("https://maven.pkg.github.com/${repoSlug.lowercase()}") {
            user.set("DanySK")
            password.set(System.getenv("GITHUB_TOKEN"))
        }
    }
    publishing.publications.withType<MavenPublication>().configureEach {
        pom {
            developers {
                developer {
                    name.set("Danilo Pianini")
                    email.set("danilo.pianini@unibo.it")
                    url.set("https://www.danilopianini.org")
                    roles.set(mutableSetOf("architect", "developer"))
                }
            }
        }
    }
}

/*
 * Root project additional configuration
 */
evaluationDependsOnChildren()

val dokkaGlobalClasspath by configurations.creating
dependencies {
    // Depend on subprojects whose presence is necessary to run
    listOf("api", "engine", "loading").forEach { api(alchemist(it)) } // Execution requirements
    with(libs.apache.commons) {
        implementation(io)
        implementation(lang3)
    }
    implementation(libs.kotlin.cli)
    implementation(libs.guava)
    implementation(libs.logback)
    testRuntimeOnly(incarnation("protelis"))
    testRuntimeOnly(incarnation("sapere"))
    testRuntimeOnly(incarnation("biochemistry"))
    testRuntimeOnly(alchemist("cognitive-agents"))
    testRuntimeOnly(alchemist("physics"))
    dokkaGlobalClasspath(alchemist("full"))
}

tasks.matching { it.name == "kotlinStoreYarnLock" }.configureEach {
    dependsOn(rootProject.tasks.named("kotlinUpgradeYarnLock"))
}

tasks.matching { it.name == "kotlinWasmStoreYarnLock" }.configureEach {
    dependsOn(rootProject.tasks.named("kotlinWasmUpgradeYarnLock"))
}

dokka {
    dokkaSourceSets.register("alldocs") {
        val submodules = checkNotNull(project.rootDir.listFiles { it.name.startsWith("alchemist-") })
        val allSourceDirs = submodules.asSequence()
            .map { it.resolve("src") }
            .onEach { check(it.isDirectory) { "Expected a directory, found a file: ${it.absolutePath}" } }
            .flatMap { sourceFolder ->
                sourceFolder.listFiles { it.name.contains("main", ignoreCase = true) }.orEmpty().asSequence()
            }.onEach { check(it.isDirectory) }
            .flatMap { sourceSetFolder ->
                sourceSetFolder.listFiles { it.name in listOf("java", "kotlin") }.orEmpty().asSequence()
            }.toList()
        sourceRoots.setFrom(allSourceDirs)
        classpath.from(dokkaGlobalClasspath)
    }
}

// WEBSITE

val websiteDir = rootProject.layout.buildDirectory.map { it.dir("website").asFile }.get()

hugo {
    version = Regex("gohugoio/hugo@v([\\.\\-\\+\\w]+)").find(file("deps-utils/action.yml").readText())!!
        .groups[1]!!
        .value
}

fun Project.dokkaCopyTask(destination: String): Copy.() -> Unit = {
    dependsOn(tasks.withType<DokkaBaseTask>())
    dependsOn(rootProject.tasks.hugoBuild)
    from(dokka.dokkaPublications.html.get().outputDirectory)
    into(File(websiteDir, "reference/$destination"))
}

val copyGlobalDokkaInTheWebsite by tasks.registering(Copy::class, dokkaCopyTask("kdoc"))
val copyModuleDokkaInTheWebsite by tasks.registering(Copy::class, alchemist("full").dokkaCopyTask("kdoc-modules"))

tasks.hugoBuild.configure {
    outputDirectory = websiteDir
    finalizedBy(copyGlobalDokkaInTheWebsite, copyModuleDokkaInTheWebsite)
}

val performWebsiteStringReplacements by tasks.registering {
    dependsOn(copyGlobalDokkaInTheWebsite, copyModuleDokkaInTheWebsite)
    doLast {
        val index = File(websiteDir, "index.html")
        require(index.exists()) {
            "file ${index.absolutePath} has been deleted."
        }
        val websiteReplacements = file("site/replacements").readLines().map { it.split("->") }.map { it[0] to it[1] }
        val replacements: List<Pair<String, String>> =
            websiteReplacements + ("!development preview!" to project.version.toString())
        index.parentFile.walkTopDown()
            .filter { it.isFile && it.extension.matches(Regex("html?", RegexOption.IGNORE_CASE)) }
            .forEach { page ->
                val initialContents = page.readText()
                var text = initialContents
                for ((toreplace, replacement) in replacements) {
                    text = text.replace(toreplace, replacement)
                }
                if (initialContents != text) {
                    page.writeText(text)
                }
            }
    }
}

tasks.hugoBuild.configure {
    finalizedBy(performWebsiteStringReplacements)
}
