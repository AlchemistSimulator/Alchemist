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
import Util.currentCommitHash
import Util.fetchJavadocIOForDependency
import Util.id
import Util.isInCI
import Util.isMac
import Util.isMultiplatform
import Util.isWindows
import org.danilopianini.gradle.mavencentral.JavadocJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.AbstractDokkaParentTask
import org.jetbrains.dokka.gradle.DokkaCollectorTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Duration

plugins {
    distribution
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.java.qa)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.hugo)
}

val minJavaVersion: String by properties

allprojects {

    with(rootProject.libs.plugins) {
        if (project.isMultiplatform) {
            apply(plugin = kotlin.multiplatform.id)
        } else {
            apply(plugin = kotlin.jvm.id)
        }
        apply(plugin = dokka.id)
        apply(plugin = gitSemVer.id)
        apply(plugin = java.qa.id)
        apply(plugin = multiJvmTesting.id)
        apply(plugin = kotlin.qa.id)
        apply(plugin = publishOnCentral.id)
        apply(plugin = taskTree.id)
    }
    apply(plugin = "distribution")

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
        // for tornadofx 2.0.0 snapshot release
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
            content {
                includeGroup("no.tornado")
            }
        }
    }

    // JVM PROJECTS CONFIGURATIONS

    if (!project.isMultiplatform) {
        dependencies {
            with(rootProject.libs) {
                compileOnly(spotbugs.annotations)
                implementation(resourceloader)
                implementation(slf4j)
                implementation(kotlin("stdlib-jdk8"))
                implementation(kotlin("reflect"))
                testCompileOnly(spotbugs.annotations)
                // Test implementation: JUnit 5 + Kotest + Mockito + Mockito-Kt + Alchemist testing tooling
                testImplementation(bundles.testing.compile)
                testImplementation(alchemist("test"))
                // Test runtime: Junit engine
                testRuntimeOnly(bundles.testing.runtimeOnly)
                // executable jar packaging
            }
            if ("incarnation" in project.name) {
                runtimeOnly(rootProject)
            }
        }

        tasks.withType<AbstractDokkaLeafTask>().configureEach {
            timeout.set(Duration.ofMinutes(5))
            dokkaSourceSets.configureEach {
                jdkVersion.set(multiJvm.jvmVersionForCompilation)
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
                            remoteUrl.set(uri(url).toURL())
                            remoteLineSuffix.set("#L")
                        }
                    }
                configurations.run { sequenceOf(api, implementation) }
                    .flatMap { it.get().dependencies }
                    .forEach { dependency ->
                        val javadocIOURLs = fetchJavadocIOForDependency(dependency)
                        if (javadocIOURLs != null) {
                            val (javadoc, packageList) = javadocIOURLs
                            externalDocumentationLink {
                                url.set(javadoc)
                                packageListUrl.set(packageList)
                            }
                        }
                    }
            }
            failOnWarning.set(true)
        }
    }

    // MULTIPLATFORM PROJECTS CONFIGURATIONS

    if (project.isMultiplatform) {
        tasks.dokkaJavadoc {
            enabled = false
        }
        tasks.withType<JavadocJar>().configureEach {
            val dokka = tasks.dokkaHtml.get()
            dependsOn(dokka)
            from(dokka.outputDirectory)
        }
        publishing {
            publications {
                publications.withType<MavenPublication>().configureEach {
                    if ("OSSRH" !in name) {
                        artifact(tasks.javadocJar)
                    }
                }
            }
        }
        /*
         * This is a workaround for the following Gradle error,
         * and should be removed as soon as possible.
         *
         * * What went wrong:
         * Execution failed for task ':dokkaHtmlCollector'.
         * > Could not determine the dependencies of null.
         * > Current thread does not hold the state lock for project ':alchemist-web-renderer'
         */
        val dokkaHtmlCollector by rootProject.tasks.named("dokkaHtmlCollector")
        dokkaHtmlCollector.dependsOn(tasks.dokkaHtml)
    }

    // COMPILE

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=all", // Enable default methods in Kt interfaces
                // Context receivers temporarily disabled, as they are unsupported in Kotlin script
                // "-Xcontext-receivers", // Enable context receivers
            )
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
                """.trimIndent(),
            )
            additionalSuppressions.set(
                """
                <suppress files=".*[\\/]expressions[\\/]parser[\\/].*" checks=".*"/>
                <suppress files=".*[\\/]biochemistrydsl[\\/].*" checks=".*"/>
                """.trimIndent(),
            )
        }
    }

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
        reports {
            create("html") { enabled = true }
        }
    }

    // PUBLISHING

    tasks.withType<Javadoc>().configureEach {
        // Disable Javadoc, use Dokka.
        enabled = false
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
        licenseName.set("GPL 3.0 with linking exception")
        licenseUrl.set("https://github.com/$repoSlug/blob/develop/LICENSE.md")
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

    // Disable distribution tasks that just clutter the build
    listOf(tasks.distZip, tasks.distTar).forEach {
        it.configure { enabled = false }
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
}

tasks.named("kotlinStoreYarnLock").configure {
    dependsOn("kotlinUpgradeYarnLock")
}

// WEBSITE

val websiteDir = project.layout.buildDirectory.map { it.dir("website").asFile }.get()

hugo {
    version = Regex("gohugoio/hugo@v([\\.\\-\\+\\w]+)")
        .find(file("deps-utils/action.yml").readText())!!.groups[1]!!.value
}

tasks {
    hugoBuild {
        outputDirectory = websiteDir
    }

// Exclude the UI and Multiplatform packages from the collector documentation.
    withType<DokkaCollectorTask>().configureEach {
        /*
         * Although the method is deprecated, no valid alternative has been implemented yet.
         * Disabling individual partial tasks has been proven ineffective.
         */
        removeChildTasks(
            allprojects.filter { it.isMultiplatform } + listOf(
                alchemist("fxui"),
                alchemist("swingui"),
            ),
        )
    }

    /**
     * Use the alchemist logo in the documentation.
     */
    val alchemistLogo = file("site/static/images/logo.svg")
    for (docTaskProvider in listOf<Provider<out AbstractDokkaParentTask>>(dokkaHtmlCollector, dokkaHtmlMultiModule)) {
        val docTask = docTaskProvider.get()
        val copyLogo = register<Copy>("copyLogoFor${docTask.name.capitalized()}") {
            from(alchemistLogo)
            into(docTask.outputDirectory.map { File(it.asFile, "images") })
            rename("logo.svg", "logo-icon.svg")
        }
        docTask.finalizedBy(copyLogo)
        hugoBuild.configure { mustRunAfter(copyLogo) }
    }

    val performWebsiteStringReplacements by registering {
        val index = File(websiteDir, "index.html")
        mustRunAfter(hugoBuild)
        if (!index.exists()) {
            logger.lifecycle("${index.absolutePath} does not exist")
            dependsOn(hugoBuild)
        }
        doLast {
            require(index.exists()) {
                "file ${index.absolutePath} existed during configuration, but it has been deleted."
            }
            val websiteReplacements = file("site/replacements").readLines()
                .map { it.split("->") }
                .map { it[0] to it[1] }
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

    mapOf("javadoc" to dokkaJavadocCollector, "kdoc" to dokkaHtmlMultiModule, "plainkdoc" to dokkaHtmlCollector)
        .mapValues { it.value.get() }
        .forEach { (folder, task) ->
            hugoBuild.configure { dependsOn(task) }
            val copyTask = register<Copy>("copy${folder.capitalized()}IntoWebsite") {
                from(task.outputDirectory)
                into(File(websiteDir, "reference/$folder"))
                finalizedBy(performWebsiteStringReplacements)
            }
            hugoBuild.configure { finalizedBy(copyTask) }
        }
}
