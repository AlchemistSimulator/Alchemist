/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
import com.github.spotbugs.SpotBugsTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

/*
 * don't ignore checkers failures
 * recheck all dependencies
 */

plugins {
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    id("org.danilopianini.git-sensitive-semantic-versioning") version Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    `java-library`
    kotlin("jvm") version Versions.org_jetbrains_kotlin
    jacoco
    id("com.github.spotbugs") version Versions.com_github_spotbugs_gradle_plugin
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint") version Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin
    `project-report`
    `build-dashboard`
    id("org.jetbrains.dokka") version Versions.org_jetbrains_dokka_gradle_plugin
    id("com.eden.orchidPlugin") version Versions.com_eden_orchidplugin_gradle_plugin
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central") version Versions.org_danilopianini_publish_on_central_gradle_plugin
    id("com.jfrog.bintray") version Versions.com_jfrog_bintray_gradle_plugin
    id("com.gradle.build-scan") version Versions.com_gradle_build_scan_gradle_plugin
}

apply(plugin = "com.gradle.build-scan")
apply(plugin = "com.eden.orchidPlugin")

allprojects {

    apply(plugin = "org.danilopianini.git-sensitive-semantic-versioning")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "project-report")
    apply(plugin = "build-dashboard")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.danilopianini.publish-on-central")
    apply(plugin = "com.jfrog.bintray")

    gitSemVer {
        version = computeGitSemVer()
    }

    repositories {
        mavenCentral()
        maven(url = "https://dl.bintray.com/kotlin/dokka/")
    }

    dependencies {
        compileOnly(Libs.annotations)
        compileOnly(Libs.spotbugs) {
            exclude(group = "commons-lang")
        }
        implementation(Libs.slf4j_api)
        implementation(Libs.kotlin_stdlib)
        implementation(Libs.kotlin_reflect)
        implementation(Libs.thread_inheritable_resource_loader)
        testCompileOnly(Libs.spotbugs) {
            exclude(group = "commons-lang")
        }
        testImplementation(Libs.junit_jupiter_api)
        testRuntimeOnly(Libs.junit_jupiter_engine)
        runtimeOnly(Libs.logback_classic)
    }

    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.name == "svgSalamander") {
                    useTarget("guru.nidi.com.kitfox:svgSalamander:1.1.2")
                    because("mapsforge version is not on central")
                }
            }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs = options.compilerArgs + listOf("-Werror")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable") // Enable default methods in Kt interfaces
            allWarningsAsErrors = true
        }
    }

    tasks.withType<Test> {
        failFast = true
        testLogging {
            events("passed", "skipped", "failed", "standardError")
            exceptionFormat = TestExceptionFormat.FULL
        }
        useJUnitPlatform()
    }

    spotbugs {
        effort = "max"
        reportLevel = "low"
        isShowProgress = true
        val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
        if (excludeFile.exists()) {
            excludeFilter = excludeFile
        }
    }

    tasks.withType<SpotBugsTask> {
        reports {
            xml.setEnabled(false)
            html.setEnabled(true)
        }
    }

    pmd {
        ruleSets = listOf()
        ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
    }

    tasks.withType<DokkaTask> {
        outputDirectory = "$buildDir/docs/javadoc"
        reportUndocumented = false
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
                    }
                    developer {
                        name.set("Roberto Casadei")
                        email.set("roby.casadei@unibo.it")
                        url.set("https://www.unibo.it/sitoweb/roby.casadei")
                    }
                }
                contributors {
                    contributor {
                        name.set("Jacob Beal")
                        email.set("jakebeal@bbn.com")
                        url.set("http://web.mit.edu/jakebeal/www/")
                    }
                    contributor {
                        name.set("Michele Bombardi")
                        email.set("michele.bombardi@studio.unibo.it")
                        url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/MicheleBombardi/")
                    }
                    contributor {
                        name.set("Elisa Casadio")
                        email.set("elisa.casadio7@studio.unibo.it")
                    }
                    contributor {
                        name.set("Chiara Casalboni")
                        email.set("chiara.casalboni2@studio.unibo.it")
                        url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/ChiaraCasalboni2/")
                    }
                    contributor {
                        name.set("Matteo Francia")
                        email.set("m.francia@unibo.it")
                    }
                    contributor {
                        name.set("Enrico Galassi")
                        email.set("enrico.galassi@studio.unibo.it")
                        url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/EnricoGalassi/")
                    }
                    contributor {
                        name.set("Gabriele Graffieti")
                        email.set("gabriele.graffieti@studio.unibo.it")
                    }
                    contributor {
                        name.set("Matteo Magnani")
                        email.set("matteo.magnani18@studio.unibo.it")
                    }
                    contributor {
                        name.set("Niccolò Maltoni")
                        email.set("niccolo.maltoni@studio.unibo.it")
                    }
                    contributor {
                        name.set("Vuksa Mihajlovic")
                        email.set("vuksa.mihajlovic@studio.unibo.it")
                    }
                    contributor {
                        name.set("Luca Mella")
                        email.set("luca.mella@studio.unibo.it")
                        url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/LucaMella/")
                    }
                    contributor {
                        name.set("Sara Montagna")
                        email.set("sara.montagna@unibo.it")
                        url.set("http://saramontagna.apice.unibo.it/")
                    }
                    contributor {
                        name.set("Luca Nenni")
                        email.set("luca.nenni@studio.unibo.it")
                        url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/LucaNenni/")
                    }
                    contributor {
                        name.set("Andrea Placuzzi")
                        email.set("andrea.placuzzi@studio.unibo.it")
                    }
                    contributor {
                        name.set("Franco Pradelli")
                        email.set("franco.pradelli@studio.unibo.it")
                    }
                    contributor {
                        name.set("Giacomo Pronti")
                        email.set("giacomo.pronti@studio.unibo.it")
                        url.set("http://apice.unibo.it/xwiki/bin/view/XWiki/GiacomoPronti/")
                    }
                    contributor {
                        name.set("Giacomo Scaparrotti")
                        email.set("giacomo.scaparrotti@studio.unibo.it")
                        url.set("https://www.linkedin.com/in/giacomo-scaparrotti-0aa77569")
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
}

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
    subprojects.forEach { api(it) }
    implementation(Libs.commons_cli)
    implementation(Libs.logback_classic)
    implementation(Libs.commons_lang3)
    implementation(Libs.ignite_core)
    orchidRuntime(Libs.orchideditorial)
    orchidRuntime(Libs.orchidkotlindoc)
    orchidRuntime(Libs.orchidplugindocs)
    orchidRuntime(Libs.orchidsearch)
    orchidRuntime(Libs.orchidsyntaxhighlighter)
    orchidRuntime(Libs.orchidwiki)
    orchidRuntime(Libs.orchidgithub)
}

tasks.withType<DokkaTask> {
    sourceDirs += subprojects.asSequence()
        .map { it.sourceSets.getByName("main") }
        .flatMap { it.allSource.srcDirs.asSequence() }
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
    val matchedVersions: List<String> = try {
        URL(baseUrl).openConnection().getInputStream().use { stream ->
            stream.bufferedReader().lineSequence()
                .flatMap { line -> versionRegex.find(line)?.groupValues?.last()?.let { sequenceOf(it) } ?: emptySequence() }
                .toList()
        }
    } catch (e: Exception) { emptyList() }
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

val orchidSeedConfiguration = "orchidSeedConfiguration"
tasks.register(orchidSeedConfiguration) {
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
        val deployMentConfiguration = if (!baseConfig.contains("services:")) {
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
        finalConfig.writeText(baseConfig + ktdocConfiguration + deployMentConfiguration)
    }
}
tasks.orchidClasses.orNull!!.dependsOn(tasks.getByName(orchidSeedConfiguration))

tasks.register<Jar>("fatJar") {
    dependsOn(subprojects.map { it.tasks.withType<Jar>() })
    manifest {
        attributes(mapOf(
            "Implementation-Title" to "Alchemist",
            "Implementation-Version" to rootProject.version,
            "Main-Class" to "it.unibo.alchemist.Alchemist",
            "Automatic-Module-Name" to "it.unibo.alchemist"
        ))
    }
    archiveBaseName.set("${rootProject.name}-redist")
    isZip64 = true
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        // remove all signature files
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
    }
    with(tasks.jar.get() as CopySpec)
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
