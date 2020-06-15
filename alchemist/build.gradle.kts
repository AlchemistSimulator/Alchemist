/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning")
    `java-library`
    kotlin("jvm")
    jacoco
    id("com.github.spotbugs")
    pmd
    checkstyle
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    `project-report`
    `build-dashboard`
    id("org.jetbrains.dokka")
    id("com.eden.orchidPlugin")
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central")
    id("com.jfrog.bintray")
    id("com.dorongold.task-tree")
    id("com.github.johnrengelman.shadow")
}

val executionRequirements = listOf("interfaces", "engine", "loading").map { project(":alchemist-$it") }

apply(plugin = "com.eden.orchidPlugin")

allprojects {

    apply(plugin = "org.danilopianini.git-sensitive-semantic-versioning")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "project-report")
    apply(plugin = "build-dashboard")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.danilopianini.publish-on-central")
    apply(plugin = "com.jfrog.bintray")
    apply(plugin = "com.dorongold.task-tree")
    apply(plugin = "com.github.johnrengelman.shadow")

    gitSemVer {
        version = computeGitSemVer()
    }

    repositories {
        // Prefer Google mirrors, they're more stable
        listOf("", "-eu", "-asia").forEach {
            maven(url = "https://maven-central$it.storage-download.googleapis.com/repos/central/data/")
        }
        mavenCentral()
        // Stuff on bintray, build-only dependencies allowed
        mapOf(
            "kotlin/dokka" to setOf("org.jetbrains.dokka"),
            "kotlin/kotlinx.html" to setOf("org.jetbrains.kotlinx"),
            "arturbosch/code-analysis" to setOf("io.gitlab.arturbosch.detekt")
        ).forEach { (uriPart, groups) ->
            maven {
                url = uri("https://dl.bintray.com/$uriPart")
                content { groups.forEach { includeGroup(it) } }
            }
        }
    }

    dependencies {
        // Code quality control
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
        // Compilation only
        compileOnly(Libs.annotations)
        compileOnly(Libs.spotbugs)
        // Implementation
        implementation(Libs.slf4j_api)
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation(Libs.thread_inheritable_resource_loader)
        // Test compilation only
        testCompileOnly(Libs.spotbugs) {
            exclude(group = "commons-lang")
        }
        // Test implementation: JUnit 5 + Kotest + Mockito + Mockito-Kt
        testImplementation(Libs.junit_jupiter_api)
        testImplementation(Libs.kotest_runner_junit5)
        testImplementation(Libs.kotest_assertions)
        testImplementation("org.mockito:mockito-core:_")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:_")
        // Test runtime: Junit engine
        testRuntimeOnly(Libs.junit_jupiter_engine)
        // executable jar packaging
        if ("incarnation" in project.name) {
            shadow(rootProject)
        }
    }

    // COMPILE

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable") // Enable default methods in Kt interfaces
            allWarningsAsErrors = true
        }
    }

    // TEST

    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed", "standardError")
            exceptionFormat = TestExceptionFormat.FULL
        }
        useJUnitPlatform()
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
        }
    }

    // CODE QUALITY

    spotbugs {
        setEffort("max")
        setReportLevel("low")
        showProgress.set(true)
        val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
        if (excludeFile.exists()) {
            excludeFilter.set(excludeFile)
        }
    }

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
        reports {
            create("html") { enabled = true }
        }
    }

    pmd {
        ruleSets = listOf()
        ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
    }

    detekt {
        failFast = true
        buildUponDefaultConfig = true
        config = files("${rootProject.projectDir}/config/detekt.yml")
        reports {
            html.enabled = true
        }
    }

    // DOCUMENTATION

    tasks.withType<DokkaTask> {
        outputDirectory = "$buildDir/docs/javadoc"
        impliedPlatforms = mutableListOf("JVM")
        // Work around https://github.com/Kotlin/dokka/issues/294
        if (!JavaVersion.current().isJava10Compatible) {
            outputFormat = "javadoc"
        }
    }

    tasks.getByName("javadocJar").dependsOn(tasks.withType<DokkaTask>())

    publishing.publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@unibo.it")
                        url.set("http://www.danilopianini.org")
                        roles.set(mutableSetOf("architect", "developer"))
                    }
                }
            }
        }
    }
    group = "it.unibo.alchemist"
    val repoSlug = "AlchemistSimulator/Alchemist.git"
    publishOnCentral {
        projectDescription.set(extra["projectDescription"].toString())
        projectLongName.set(extra["projectLongName"].toString())
        licenseName.set("GPL 3.0 with linking exception")
        licenseUrl.set("https://github.com/AlchemistSimulator/Alchemist/blob/develop/LICENSE.md")
        scmConnection.set("git:git@github.com:$repoSlug")
    }
    val apiKeyName = "BINTRAY_API_KEY"
    val userKeyName = "BINTRAY_USER"
    bintray {
        user = System.getenv(userKeyName)
        key = System.getenv(apiKeyName)
        setPublications("mavenCentral")
        override = true
        with(pkg) {
            repo = "Alchemist"
            name = project.name
            userOrg = "alchemist-simulator"
            vcsUrl = "https://github.com/$repoSlug"
            setLicenses("GPL-3.0-or-later")
            with(version) {
                name = project.version.toString()
            }
        }
    }
    tasks.withType<BintrayUploadTask> {
        onlyIf {
            val hasKey = System.getenv(apiKeyName) != null
            val hasUser = System.getenv(userKeyName) != null
            if (!hasKey) {
                println("The $apiKeyName environment variable must be set in order for the bintray deployment to work")
            }
            if (!hasUser) {
                println("The $userKeyName environment variable must be set in order for the bintray deployment to work")
            }
            hasKey && hasUser
        }
    }

    // Shadow Jar
    tasks.withType<ShadowJar> {
        manifest {
            attributes(mapOf(
                "Implementation-Title" to "Alchemist",
                "Implementation-Version" to rootProject.version,
                "Main-Class" to "it.unibo.alchemist.Alchemist",
                "Automatic-Module-Name" to "it.unibo.alchemist"
            ))
        }
        exclude("META-INF/")
        exclude("ant_tasks/")
        exclude("about_files/")
        exclude("help/about/")
        exclude("build")
        exclude(".gradle")
        exclude("build.gradle")
        exclude("gradle")
        exclude("gradlew")
        exclude("gradlew.bat")
        isZip64 = true
        destinationDirectory.set(file("${rootProject.buildDir}/libs"))
    }
}

/*
 * Root project additional configuration
 */
evaluationDependsOnChildren()

repositories {
    mavenCentral()
    jcenter {
        content {
            includeGroupByRegex("""io\.github\.javaeden.*""")
            includeGroupByRegex("""com\.eden.*""")
            includeModuleByRegex("""org\.jetbrains\.kotlinx""", """kotlinx-serialization.*""")
        }
    }
}

dependencies {
    executionRequirements.forEach { api(it) }
    implementation(Libs.commons_io)
    implementation(Libs.commons_cli)
    implementation(Libs.logback_classic)
    implementation(Libs.commons_lang3)
    runtimeOnly(Libs.logback_classic)
    testRuntimeOnly(project(":alchemist-incarnation-protelis"))
    orchidRuntimeOnly(Libs.orchideditorial)
    orchidRuntimeOnly(Libs.orchidkotlindoc)
    orchidRuntimeOnly(Libs.orchidplugindocs)
    orchidRuntimeOnly(Libs.orchidsearch)
    orchidRuntimeOnly(Libs.orchidsyntaxhighlighter)
    orchidRuntimeOnly(Libs.orchidwiki)
    orchidRuntimeOnly(Libs.orchidgithub)
}

// WEBSITE

tasks.withType<DokkaTask> {
    subProjects = subprojects.map { it.name }.toList()
}

val isMarkedStable by lazy { """\d+(\.\d+){2}""".toRegex().matches(rootProject.version.toString()) }

orchid {
    theme = "Editorial"
    // Feed arguments to Kdoc
//    val projects: Collection<Project> = listOf(project) + subprojects
//    val paths = projects.map { it.sourceSets["main"].compileClasspath.asPath }
//    args = listOf("--kotlindocClasspath") + paths.joinToString(File.pathSeparator)
    // Determine whether it's a deployment or a dry run
    baseUrl = "https://alchemistsimulator.github.io/${if (isMarkedStable) "" else "latest/"}"
    // Fetch the latest version of the website, if this one is more recent enable deploy
    val versionRegex = """.*Currently\s*(.+)\.\s*Created""".toRegex()
    val matchedVersions: List<String> = runCatching {
        URL(baseUrl).openConnection().getInputStream().use { stream ->
            stream.bufferedReader().lineSequence()
                .flatMap { line ->
                    versionRegex.find(line)?.groupValues?.last()?.let { sequenceOf(it) } ?: emptySequence()
                }
                .toList()
        }
    }.getOrDefault(emptyList())
    val shouldDeploy = matchedVersions
        .takeIf { it.size == 1 }
        ?.first()
        ?.let { rootProject.version.toString() > it }
        ?: false
    dryDeploy = shouldDeploy.not().toString()
    println(
        when (matchedVersions.size) {
            0 -> "Unable to fetch the current site version from $baseUrl"
            1 -> "Website $baseUrl is at version ${matchedVersions.first()}"
            else -> "Multiple site versions fetched from $baseUrl: $matchedVersions"
        } + ". Orchid deployment ${if (shouldDeploy) "enabled" else "set as dry run"}."
    )
}

val orchidSeedConfiguration by tasks.register("orchidSeedConfiguration") {
    doLast {
        /*
         * Detect files
         */
        val configFolder = listOf(projectDir.toString(), "src", "orchid", "resources")
            .joinToString(separator = File.separator)
        val baseConfig = file("$configFolder${File.separator}config-origin.yml").readText()
        val finalConfig = file("$configFolder${File.separator}config.yml")
        /*
         * Compute Kdoc targets
         */
        val ktdocConfiguration = if (!baseConfig.contains("kotlindoc:")) {
            val sourceFolders = allprojects.asSequence()
                .flatMap { it.sourceSets["main"].allSource.srcDirs.asSequence() }
                .map { it.toString().replace("$projectDir/", "../../../") }
                .map { "\n    - '$it'" }
                .joinToString(separator = "")
            """
                kotlindoc:
                  menu:
                    - type: "kotlindocClassLinks"
                      includeItems: true
                  pages:
                    extraCss:
                      - 'assets/css/orchidKotlindoc.scss'
                  sourceDirs:
            """.trimIndent() + sourceFolders + "\n"
        } else ""
        val deploymentConfiguration = if (!baseConfig.contains("services:")) {
            """
                services:
                  publications:
                    stages:
                      - type: githubPages
                        username: 'DanySK'
                        commitUsername: Danilo Pianini
                        commitEmail: danilo.pianini@gmail.com
                        repo: 'AlchemistSimulator/${if (isMarkedStable) "alchemistsimulator.github.io" else "latest" }'
                        branch: ${if (isMarkedStable) "master" else "gh-pages"}
                        publishType: CleanBranchMaintainHistory
            """.trimIndent()
        } else ""
        finalConfig.writeText(baseConfig + ktdocConfiguration + deploymentConfiguration)
    }
}
tasks.orchidClasses.orNull!!.dependsOn(orchidSeedConfiguration)
