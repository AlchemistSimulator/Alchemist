/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

import Libs.alchemist
import Util.isInCI
import Util.isMac
import Util.isMultiplatform
import Util.isWindows
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.JPackageTask

plugins {
    application
    alias(libs.plugins.jpackage)
}

dependencies {
    runtimeOnly(rootProject)
    rootProject.subprojects.filterNot { it == project }.forEach {
        if (it.isMultiplatform) {
            runtimeOnly(project(path = ":${it.name}", configuration = "default"))
        } else {
            runtimeOnly(it)
        }
    }
    testImplementation(rootProject)
    testImplementation(alchemist("physics"))
}

application {
    mainClass.set("it.unibo.alchemist.Alchemist")
}

val copyForPackaging by tasks.registering(Copy::class) {
    val jarFile = tasks.shadowJar.get().archiveFileName.get()
    from("${rootProject.projectDir}/build/shadow/$jarFile")
    into("${rootProject.projectDir}/build/package-input")
    dependsOn(tasks.shadowJar)
}

open class CustomJPackageTask() : JPackageTask() {
    @TaskAction
    override fun action() {
        var types: List<ImageType>
        when {
            isWindows -> types = listOf(ImageType.EXE, ImageType.MSI)
            isMac -> types = listOf(ImageType.DMG, ImageType.PKG)
            else -> types = listOf(ImageType.DEB, ImageType.RPM)
        }
        types.forEach {
            setType(it)
            super.action()
        }
    }
}

// jpackageFull should be used instead
tasks.jpackage {
    enabled = false
}

val jpackageFull by tasks.registering(CustomJPackageTask::class) {
    group = "Distribution"
    description = "Creates application bundle in every supported type using jpackage"
    // General info
    resourceDir = "${project.projectDir}/package-settings"
    appName = rootProject.name
    appVersion = rootProject.version.toString().substringBefore('-')
    copyright = "Copyright (C) 2010-2023, Danilo Pianini and contributors"
    appDescription = rootProject.description
    vendor = ""
    licenseFile = "${rootProject.projectDir}/LICENSE.md"
    verbose = isInCI

    // Packaging settings
    input = rootProject.layout.buildDirectory.map { it.dir("package-input") }.get().asFile.path
    destination = rootProject.layout.buildDirectory.map { it.dir("package") }.get().asFile.path
    mainJar = tasks.shadowJar.get().archiveFileName.get()
    mainClass = application.mainClass.get()

    linux {
        icon = "${project.projectDir}/package-settings/logo.png"
    }
    windows {
        icon = "${project.projectDir}/package-settings/logo.ico"
        winDirChooser = true
        winShortcutPrompt = true
        winPerUserInstall = isInCI
    }
    mac {
        icon = "${project.projectDir}/package-settings/logo.png"
    }
    dependsOn(copyForPackaging)
}

val deleteJpackageOutput by tasks.registering(Delete::class) {
    setDelete(project.file("build/package/install"))
}

tasks.register<Exec>("testJpackageOutput") {
    group = "Verification"
    description = "Verifies the jpackage output correctness for the OS running the script"
    isIgnoreExitValue = true
    workingDir = rootProject.file("build/package/")
    doFirst {
        val version = rootProject.version.toString().substringBefore('-')
        // Extract the packet
        when {
            isWindows -> commandLine("msiexec", "-i", "${rootProject.name}-$version.msi", "-quiet", "INSTALLDIR=${workingDir.path}\\install")
            isMac -> commandLine("sudo", "installer", "-pkg", "${rootProject.name}-$version.pkg", "-target", "/")
            else -> {
                workingDir.resolve("install").mkdirs()
                commandLine("bsdtar", "-xf", "${rootProject.name}-$version-1.x86_64.rpm", "-C", "install")
            }
        }
    }
    doLast {
        // Check if package contains every file needed
        var execFiles: List<String>
        var appFiles: List<String>
        when {
            isWindows -> {
                execFiles = workingDir.resolve("install").listFiles().map { it.name }
                appFiles = workingDir.resolve("install/app").listFiles().map { it.name }
            }
            isMac -> {
                val root = File("/Applications/${rootProject.name}.app")
                execFiles = root.resolve("Contents/MacOS").listFiles().map { it.name }
                appFiles = root.resolve("Contents/app").listFiles().map { it.name }
            }
            else -> {
                execFiles = workingDir.resolve("install/opt/alchemist/bin").listFiles().map { it.name }
                appFiles = workingDir.resolve("install/opt/alchemist/lib/app").listFiles().map { it.name }
            }
        }
        require(rootProject.name in execFiles || "${rootProject.name}.exe" in execFiles)
        require(jpackageFull.get().mainJar in appFiles)
    }
    mustRunAfter(jpackageFull)
    finalizedBy(deleteJpackageOutput)
}

tasks.withType<AbstractArchiveTask> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Angelo Filaseta")
                    email.set("angelo.filaseta@studio.unibo.it")
                }
            }
        }
    }
}
