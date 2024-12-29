/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import Libs.alchemist
import Libs.incarnation
import Util.allVerificationTasks
import Util.id
import Util.isInCI
import Util.isMac
import Util.isWindows
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask

plugins {
    id("kotlin-jvm-convention")
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.java.qa)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.hugo)
}

val minJavaVersion: String by properties

allprojects {

    with(rootProject.libs.plugins) {
        apply(plugin = gitSemVer.id)
        apply(plugin = java.qa.id)
        apply(plugin = multiJvmTesting.id)
        apply(plugin = kotlin.qa.id)
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

    javaQA {
        checkstyle {
            additionalConfiguration.set(rootProject.file("checkstyle-additional-config.xml").readText())
            additionalSuppressions.set(
                """
                <suppress files=".*[\\/]expressions[\\/]parser[\\/].*" checks=".*"/>
                <suppress files=".*[\\/]biochemistrydsl[\\/].*" checks=".*"/>
                """.trimIndent(),
            )
        }
        // TODO: enable PMD when this bug is fixed: https://github.com/pmd/pmd/issues/5096
        tasks.withType<Pmd>().configureEach {
            enabled = false
        }
    }

    // TEST AND COVERAGE

    tasks.withType<Test>().configureEach {
        testLogging {
            events("passed", "skipped", "failed", "standardError")
            exceptionFormat = TestExceptionFormat.FULL
        }
        useJUnitPlatform()
        maxHeapSize = "1g"
    }

    // CODE QUALITY

    tasks.allVerificationTasks.configureEach {
        exclude { "generated" in it.file.absolutePath }
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

    // Dokka dependencies
    subprojects.forEach { dokka(it) }
}

tasks.matching { it.name == "kotlinStoreYarnLock" }.configureEach {
    dependsOn(rootProject.tasks.named("kotlinUpgradeYarnLock"))
}

// WEBSITE

val websiteDir =
    project.layout.buildDirectory
        .map { it.dir("website").asFile }
        .get()

hugo {
    version =
        Regex("gohugoio/hugo@v([\\.\\-\\+\\w]+)")
            .find(file("deps-utils/action.yml").readText())!!
            .groups[1]!!
            .value
}

val copyDokkaInTheWebsite by tasks.registering(Copy::class) {
    dependsOn(tasks.withType<DokkaBaseTask>())
    dependsOn(tasks.hugoBuild)
    tasks.hugoBuild
    from(
        dokka.dokkaPublications.html
            .get()
            .outputDirectory,
    )
    into(File(websiteDir, "reference/kdoc"))
}

tasks.hugoBuild.configure {
    outputDirectory = websiteDir
    finalizedBy(copyDokkaInTheWebsite)
}

val performWebsiteStringReplacements by tasks.registering {
    val index = File(websiteDir, "index.html")
    mustRunAfter(copyDokkaInTheWebsite)
    if (!index.exists()) {
        logger.lifecycle("${index.absolutePath} does not exist")
        dependsOn(copyDokkaInTheWebsite)
    }
    doLast {
        require(index.exists()) {
            "file ${index.absolutePath} existed during configuration, but it has been deleted."
        }
        val websiteReplacements =
            file("site/replacements")
                .readLines()
                .map { it.split("->") }
                .map { it[0] to it[1] }
        val replacements: List<Pair<String, String>> =
            websiteReplacements + ("!development preview!" to project.version.toString())
        index.parentFile
            .walkTopDown()
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

copyDokkaInTheWebsite.configure {
    finalizedBy(performWebsiteStringReplacements)
}
