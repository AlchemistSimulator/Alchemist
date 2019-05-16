/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
import com.github.spotbugs.SpotBugsTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Kotlin migration TODO list
 *
 * upgrade to junit5
 * farjar plugin?
 * switch to orchid kotlindoc https://orchid.netlify.com/plugins/OrchidKotlindoc
 * dokka-merge-plugin?
 * update dependencies
 * don't ignore checkers failures
 * recheck all dependencies
 */

plugins {
    id("de.fayard.buildSrcVersions") version
            Versions.de_fayard_buildsrcversions_gradle_plugin
    id("org.danilopianini.git-sensitive-semantic-versioning") version
            Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    `java-library`
    kotlin("jvm") version
            Versions.org_jetbrains_kotlin
    jacoco
    id("com.github.spotbugs") version
            Versions.com_github_spotbugs_gradle_plugin
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint") version
            Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin
    `project-report`
    `build-dashboard`
    id("org.jetbrains.dokka") version
            Versions.org_jetbrains_dokka_gradle_plugin
    id("org.danilopianini.javadoc.io-linker") version
            Versions.org_danilopianini_javadoc_io_linker_gradle_plugin
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central") version
            Versions.org_danilopianini_publish_on_central_gradle_plugin
    id("com.jfrog.bintray") version
            Versions.com_jfrog_bintray_gradle_plugin
    id("com.gradle.build-scan") version
            Versions.com_gradle_build_scan_gradle_plugin
}

apply(plugin = "project-report")

allprojects {

    if (!JavaVersion.current().isJava11Compatible) {
        project.version = project.version.toString() + "-j8"
    }
    extra["scalaVersion"] = "${extra["scalaMajorVersion"]}.${extra["scalaMinorVersion"]}"

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

    configurations {
        all {
            if (!name.contains("antlr")) {
                resolutionStrategy {
                    force("org.antlr:antlr-runtime:${extra["antlrRuntimeVersion"]}")
                }
            }
        }
    }

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation(Libs.commons_io)
        implementation(Libs.commons_math3)
        implementation(Libs.commons_lang3)
        implementation(Libs.guava)
        implementation(Libs.annotations)
        implementation(Libs.spotbugs)
        implementation(Libs.slf4j_api)
        implementation(Libs.kotlin_stdlib)
        implementation(Libs.kotlin_reflect)
        implementation(Libs.thread_inheritable_resource_loader)
        testImplementation(Libs.junit)
        runtimeOnly(Libs.logback_classic)
//        doclet(Libs.apiviz)
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable")
        }
    }

    tasks.withType<Test> {
        failFast = true
        testLogging { events("passed", "skipped", "failed", "standardError") }
    }

    spotbugs {
        isIgnoreFailures = true
        effort = "max"
        reportLevel = "low"
        val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
        if (excludeFile.exists()) {
            excludeFilterConfig = project.resources.text.fromFile(excludeFile)
        }
    }

    tasks.withType<SpotBugsTask> {
        reports {
            xml.setEnabled(false)
            html.setEnabled(true)
        }
    }

    pmd {
        setIgnoreFailures(true)
        ruleSets = listOf()
        ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
    }

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
    val apiKeyName = "BINTRAY_API_KEY"
    val userKeyName = "BINTRAY_USER"
    bintray {
        user = System.getenv(userKeyName)
        key = System.getenv(apiKeyName)
        setPublications("mavenCentral")
        override = true
        with(pkg) {
            repo = extra["longName"].toString()
            name = project.name
            userOrg = "alchemist-simulator"
            vcsUrl = "${extra["scmRootUrl"]}/${extra["scmRepoName"]}"
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

    /*
     * Configure Dokka to run before Javadoc, so that Kotlin classes are correctly documented
     * and doclet still applied for Java classes. Then copy missing files and lowercase files
     * to the javadoc folder.
     */
    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        dependsOn(tasks.withType<DokkaTask>())
    }
    tasks.withType<DokkaTask> {
        if (JavaVersion.current().isJava9Compatible) {
            enabled = false
        }
        outputDirectory = "$buildDir/dokka"
        reportUndocumented = false
        impliedPlatforms = mutableListOf("JVM")
        outputFormat = "javadoc"
    }
    val dokka = tasks.findByName("dokka") as DokkaTask
    val javadoc = tasks.findByName("javadoc") as Javadoc
    tasks.register<Copy>("fillDocs") {
        dependsOn(dokka)
        dependsOn(javadoc)
        from(dokka.outputDirectory)
        into(javadoc.destinationDir!!)
        eachFile {
            if (relativePath.getFile(destinationDir).exists()) {
                exclude()
            }
        }
    }
    tasks.register<Copy>("makeDocs") {
        val fillDocs = tasks.findByName("fillDocs")
        dependsOn(fillDocs)
        from(dokka.outputDirectory)
        into(javadoc.destinationDir!!)
        eachFile {
            if (Character.isUpperCase(name[0])) {
                exclude()
            }
        }
    }
    val makeDocs = tasks.findByName("makeDocs")
    javadoc.finalizedBy(makeDocs)
    dokka.finalizedBy(makeDocs)
}

subprojects.forEach { subproject -> rootProject.evaluationDependsOn(subproject.path) }

/*
 * Running a task on the parent project implies running the same task first on any subproject
 */
tasks.forEach { task ->
    subprojects.forEach { subproject ->
        val subtask = subproject.tasks.findByPath(task.name)
        if (subtask != null) {
            task.dependsOn(subtask)
        }
    }
}

dependencies {
    subprojects.forEach { api(it) }
    implementation(Libs.commons_cli)
    implementation(Libs.logback_classic)
    implementation(Libs.commons_lang3)
    implementation(Libs.ignite_core)
}

tasks.withType<Javadoc> {
    val subprojectJavadocs = subprojects.flatMap { it.tasks.withType<Javadoc>() }
    dependsOn(subprojectJavadocs)
    source(subprojectJavadocs.map { it.source })
    classpath += subprojects.asSequence()
        .flatMap { it.sourceSets.getByName("main").runtimeClasspath.files.asSequence() }
        .toList().toTypedArray()
        .let { files(*it) }
}

tasks.withType<DokkaTask> {
    sourceDirs += subprojects.asSequence()
        .map { it.sourceSets.getByName("main") }
        .flatMap { it.allSource.srcDirs.asSequence() }
}

allprojects {
    val jdocTasks = listOf("javadoc", "uploadArchives", "projectReport", "buildDashboard", "javadocJar")
    val selectedTasks = gradle.startParameter.taskNames
    if (!jdocTasks.any { selectedTasks.contains(it) }) {
        apply(plugin = "org.danilopianini.javadoc.io-linker")
    }
}

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

apply(plugin = "com.gradle.build-scan")
buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

defaultTasks("clean", "test", "check", "makeDocs", "projectReport", "buildDashboard", "fatJar", "sign")
