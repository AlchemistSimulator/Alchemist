/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
import Libs.incarnation
import Libs.alchemist
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.danilopianini.gradle.mavencentral.mavenCentral
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.net.URL

plugins {
    kotlin("jvm")
    id("org.danilopianini.git-sensitive-semantic-versioning")
    id("org.danilopianini.publish-on-central")
    id("org.jetbrains.dokka")
    alias(libs.plugins.java.qa)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.orchid)
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.taskTree)
}

apply(plugin = rootProject.libs.plugins.orchid.id)

val additionalTools: Configuration by configurations.creating
dependencies {
    additionalTools("org.jacoco:org.jacoco.core:_")
}

val Provider<PluginDependency>.id get() = get().pluginId

allprojects {

    with(rootProject.libs.plugins) {
        apply(plugin = java.qa.id)
        apply(plugin = multiJvmTesting.id)
        apply(plugin = kotlin.qa.id)
        apply(plugin = taskTree.id)
        apply(plugin = shadowJar.id)
    }

    apply(plugin = "org.danilopianini.git-sensitive-semantic-versioning")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "build-dashboard")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.danilopianini.publish-on-central")

    multiJvm {
        jvmVersionForCompilation.set(11)
        maximumSupportedJvmVersion.set(latestJava)
    }

    repositories {
        google()
        mavenCentral()
        jcenter {
            content {
                onlyForConfigurations(
                    "orchidCompileClasspath",
                    "orchidRuntimeClasspath"
                )
            }
        }

        // for tornadofx 2.0.0 snapshot release
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
            content {
                includeGroup("no.tornado")
            }
        }
    }

    dependencies {
        // Compilation only
        compileOnly(Libs.annotations)
        compileOnly(Libs.spotBugsModule("annotations"))
        // Implementation
        implementation(Libs.slf4j_api)
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation(Libs.thread_inheritable_resource_loader)
        // Test compilation only
        testCompileOnly(Libs.spotBugsModule("annotations"))
        // Test implementation: JUnit 5 + Kotest + Mockito + Mockito-Kt
        testImplementation(Libs.junit("api"))
        testImplementation(Libs.kotest_runner_junit5)
        testImplementation(Libs.kotest_assertions)
        testImplementation("org.mockito:mockito-core:_")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:_")
        // Test runtime: Junit engine
        testRuntimeOnly(Libs.junit("engine"))
        // executable jar packaging
        if ("incarnation" in project.name) {
            runtimeOnly(rootProject)
        }
    }

    // COMPILE

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all") // Enable default methods in Kt interfaces
        }
    }

    // TEST AND COVERAGE

    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed", "standardError")
            exceptionFormat = TestExceptionFormat.FULL
        }
        useJUnitPlatform()
    }

    // CODE QUALITY

    javaQA {
        checkstyle {
            additionalConfiguration.set(
                """
                <module name="RegexpSingleline">
                    <property name="severity" value="error" />
                    <property name="format" value="Math\s*\.\s*random\s*\(\s*\)" />
                    <property name="fileExtensions" value="java,xtend,scala,kt" />
                    <property name="message"
                              value="Don't use Math.random() inside Alchemist. Breaks stuff." />
                </module>
                <module name="RegexpSingleline">
                    <property name="severity" value="error" />
                    <property name="format" value="class\s*\.\s*forName\s*\(" />
                    <property name="fileExtensions" value="java,xtend,scala,kt" />
                    <property name="message"
                              value="Use the library to load classes and resources. Breaks grid otherwise." />
                </module>
                <module name="RegexpSingleline">
                    <property name="severity" value="error" />
                    <property name="format" value="class\s*\.\s*getResource" />
                    <property name="fileExtensions" value="java,xtend,scala,kt" />
                    <property name="message"
                              value="Use the library to load classes and resources. Breaks grid otherwise." />
                </module>
                <module name="RegexpSingleline">
                    <property name="severity" value="error" />
                    <property name="format" value="class\s*\.\s*getClassLoader" />
                    <property name="fileExtensions" value="java,xtend,scala,kt" />
                    <property name="message"
                              value="Use the library to load classes and resources. Breaks grid otherwise." />
                </module>
                <module name="RegexpSingleline">
                    <property name="severity" value="warning" />
                    <property name="format" value="@author" />
                    <property name="fileExtensions" value="java,xtend,scala,kt" />
                    <property name="message"
                              value="Do not use @author. Changes and authors are tracked by the content manager." />
                </module>
                """.trimIndent()
            )
            additionalSuppressions.set(
                """
                <suppress files=".*[\\/]expressions[\\/]parser[\\/].*" checks=".*"/>
                <suppress files=".*[\\/]biochemistrydsl[\\/].*" checks=".*"/>
                """.trimIndent()
            )
        }
    }

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
        reports {
            create("html") { enabled = true }
        }
    }

    // PUBLISHING

    tasks.withType<Javadoc> {
        // Disable Javadoc, use Dokka.
        enabled = false
    }

    if (System.getenv("CI") == true.toString()) {
        signing {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }

    group = "it.unibo.alchemist"
    val repoSlug = "AlchemistSimulator/Alchemist.git"
    publishOnCentral {
        projectDescription = extra["projectDescription"].toString()
        projectLongName = extra["projectLongName"].toString()
        licenseName = "GPL 3.0 with linking exception"
        licenseUrl = "https://github.com/AlchemistSimulator/Alchemist/blob/develop/LICENSE.md"
        scmConnection = "git:git@github.com:$repoSlug"
        repository("https://maven.pkg.github.com/alchemistsimulator/alchemist") {
            user = "DanySK"
            password = System.getenv("GITHUB_TOKEN")
        }
        repository("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/", "CentralS01") {
            user = mavenCentral().user()
            password = mavenCentral().password()
        }
    }
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

    // Shadow Jar
    tasks.withType<ShadowJar> {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to "Alchemist",
                    "Implementation-Version" to rootProject.version,
                    "Main-Class" to "it.unibo.alchemist.Alchemist",
                    "Automatic-Module-Name" to "it.unibo.alchemist"
                )
            )
        }
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
        mergeServiceFiles()
        destinationDirectory.set(file("${rootProject.buildDir}/shadow"))
        if ("full" in project.name || "incarnation" in project.name || project == rootProject) {
            // Run the jar and check the output
            val testShadowJar = tasks.register<Exec>("${this.name}-testWorkingOutput") {
                val javaExecutable = org.gradle.internal.jvm.Jvm.current().javaExecutable.absolutePath
                val command = arrayOf(javaExecutable, "-jar", archiveFile.get().asFile.absolutePath, "--help")
                commandLine(*command)
                val interceptOutput = ByteArrayOutputStream()
                val interceptError = ByteArrayOutputStream()
                standardOutput = interceptOutput
                errorOutput = interceptError
                isIgnoreExitValue = true
                doLast {
                    val exit = executionResult.get().exitValue
                    require(exit == 0) {
                        val outputs = listOf(interceptOutput, interceptError).map {
                            String(it.toByteArray(), Charsets.UTF_8)
                        }
                        outputs.forEach { text ->
                            for (illegalKeyword in listOf("SLF4J", "NOP")) {
                                require(illegalKeyword !in text) {
                                    """
                                $illegalKeyword found while printing the help. Complete output:
                                $text
                                    """.trimIndent()
                                }
                            }
                        }
                        """
                            Process '${command.joinToString(" ")}' exited with $exit
                            Output:
                            ${outputs[0]}
                            Error:
                            ${outputs[0]}
                        """.trimIndent()
                    }
                }
            }
            this.finalizedBy(testShadowJar)
        }
    }
}

/*
 * Root project additional configuration
 */
evaluationDependsOnChildren()

dependencies {
    // Depend on subprojects whose presence is necessary to run
    listOf("interfaces", "engine", "loading") // Execution requirements
        .map { project(":alchemist-$it") }
        .forEach { api(it) }
    implementation(Libs.apacheCommons("io"))
    implementation(Libs.apacheCommons("lang3"))
    implementation(Libs.apacheCommons("cli"))
    implementation(Libs.logback_classic)
    testRuntimeOnly(incarnation("protelis"))
    testRuntimeOnly(incarnation("sapere"))
    testRuntimeOnly(incarnation("biochemistry"))
    testRuntimeOnly(alchemist("cognitive-agents"))
    testRuntimeOnly(alchemist("physical-agents"))

    // Populate the dependencies for Orchid
    orchidImplementation(libs.orchid.core)
    orchidRuntimeOnly(libs.bundles.orchid)
}

// WEBSITE

fun String.toVersion() = org.danilopianini.gradle.gitsemver.SemanticVersion.fromStringOrNull(this)
    ?: throw IllegalStateException("Not a valid semantic version: $this")

val projectVersion = gitSemVer.computeVersion().toVersion()

@ExperimentalUnsignedTypes
val isMarkedStable = projectVersion.preRelease.isEmpty()

tasks.withType<org.jetbrains.dokka.gradle.DokkaCollectorTask> {
    val type = Regex("^dokka(\\w+)Collector\$").matchEntire(name)?.destructured?.component1()?.toLowerCase()
        ?: throw IllegalStateException("task named $name does not match the expected name pattern for dokka collection tasks")
    if (type.equals("html", ignoreCase = true) || type.equals("javadoc", ignoreCase = true)) {
        // Bind Dokka Javadoc and Dokka HTML to Orchid
        outputDirectory.set(file("$buildDir/docs/orchid/$type"))
        listOf(tasks.orchidServe, tasks.orchidBuild).forEach { it.get().dependsOn(this) }
    }
}

orchid {
    theme = "Editorial"
    // Determine whether it's a deployment or a dry run
    baseUrl = "https://alchemistsimulator.github.io/${if (isMarkedStable) "" else "latest/"}"
    // Fetch the latest version of the website, if this one is more recent enable deploy
    val versionRegex =
        """.*Currently\s*(.+)\.\s*Created""".toRegex()
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
        ?.let { projectVersion > it.toVersion() }
        ?: false
    githubToken = System.getenv("githubToken")
        ?: project.findProperty("githubToken")?.toString()
        ?: System.getenv("GITHUB_TOKEN")
    dryDeploy = shouldDeploy.not().toString()
    println(
        when (matchedVersions.size) {
            0 -> "Unable to fetch the current site version from $baseUrl"
            1 -> "Website $baseUrl is at version ${matchedVersions.first()}"
            else -> "Multiple site versions fetched from $baseUrl: $matchedVersions"
        } + ". Orchid deployment ${if (shouldDeploy) "enabled" else "set as dry run"}."
    )
}

gradle.taskGraph.whenReady {
    if (hasTask(tasks.orchidDeploy.get()) &&
        orchid.dryDeploy?.toBoolean()?.not() == true &&
        orchid.githubToken.isNullOrBlank()
    ) {
        throw IllegalStateException("Real deployment requested but no GitHub deployment token set")
    }
}

val orchidSeedConfiguration by tasks.register("orchidSeedConfiguration") {
    /*
     * Detect files
     */
    val configFolder = listOf(projectDir.toString(), "src", "orchid", "resources")
        .joinToString(separator = File.separator)
    val baseConfigFile = file("$configFolder${File.separator}config-origin.yml")
    @org.gradle.api.tasks.InputFile
    fun baseConfig(): File = baseConfigFile
    val finalConfig = file("$configFolder${File.separator}config.yml")
    @org.gradle.api.tasks.OutputFile
    fun finalConfig(): File = finalConfig
    doLast {
        /*
         * Compute Kdoc targets
         */
        val baseConfig = baseConfigFile.readText()
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
        finalConfig.writeText(baseConfig + deploymentConfiguration)
    }
}
tasks.orchidClasses.orNull!!.dependsOn(orchidSeedConfiguration)
