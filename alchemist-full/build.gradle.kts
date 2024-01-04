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
import Util.isUnix
import Util.isWindows
import org.panteleyev.jpackage.ImageType
import java.io.ByteArrayOutputStream

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

// JPackage
tasks.jpackage {
    // General info
    resourceDir = "${rootProject.projectDir}/package-settings"
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
    mainJar = "alchemist-full-${rootProject.version}-all.jar"
    mainClass = "it.unibo.alchemist.Alchemist"

    linux {
        icon = "${rootProject.projectDir}/package-settings/logo.png"
        type = ImageType.RPM
    }
    windows {
        icon = "${rootProject.projectDir}/package-settings/logo.ico"
        type = ImageType.MSI
        winDirChooser = true
        winShortcutPrompt = true
        winPerUserInstall = isInCI
    }
    mac {
        icon = "${rootProject.projectDir}/package-settings/logo.png"
        type = ImageType.PKG
    }

    dependsOn("copyForPackaging")
}

tasks.register<Copy>("copyForPackaging") {
    val jarFile = "alchemist-full-${rootProject.version}-all.jar"
    from("${rootProject.projectDir}/build/shadow/$jarFile")
    into("${rootProject.projectDir}/build/package-input")
    dependsOn("shadowJar")
}

tasks.register<Exec>("testJpackageOutput") {
    group = "Verification"
    description = "Verifies the jpackage output correctness for the OS running the script"
    val interceptOutput = ByteArrayOutputStream()
    val interceptError = ByteArrayOutputStream()
    val version = rootProject.version.toString().substringBefore('-')
    val isLinux = isUnix && !isMac
    standardOutput = interceptOutput
    errorOutput = interceptError
    isIgnoreExitValue = true
    workingDir = rootProject.file("build/package/")
    doFirst {
        // Extract the packet
        if (isWindows) {
            commandLine("msiexec", "-i", "${rootProject.name}-$version.msi", "-quiet", "INSTALLDIR=${workingDir.path}\\install")
        } else if (isMac) {
            commandLine("sudo", "installer", "-pkg", "${rootProject.name}-$version.pkg", "-target", "/")
        } else if (isLinux) {
            workingDir.resolve("install").mkdirs()
            commandLine("bsdtar", "-xf", "${rootProject.name}-$version-1.x86_64.rpm", "-C", "install")
        }
    }
    doLast {
        // Check if package contains every file needed
        var execFiles = listOf("")
        var appFiles = listOf("")
        if (isWindows) {
            execFiles = workingDir.resolve("install").listFiles().map { it.name }
            appFiles = workingDir.resolve("install/app").listFiles().map { it.name }
        } else if (isMac) {
            val root = File("/Applications/${rootProject.name}.app")
            execFiles = root.resolve("Contents/MacOS").listFiles().map { it.name }
            appFiles = root.resolve("Contents/app").listFiles().map { it.name }
        } else if (isLinux) {
            execFiles = workingDir.resolve("install/opt/alchemist/bin").listFiles().map { it.name }
            appFiles = workingDir.resolve("install/opt/alchemist/lib/app").listFiles().map { it.name }
        }
        require(rootProject.name in execFiles || "${rootProject.name}.exe" in execFiles)
        require(tasks.jpackage.get().mainJar in appFiles)
    }

    dependsOn(tasks.jpackage)
    finalizedBy("deleteJpackageOutput")
}

tasks.register<Delete>("deleteJpackageOutput") {
    setDelete(project.file("build/package/install"))
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
