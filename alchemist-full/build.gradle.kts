/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */
import Libs.alchemist
import Util.commandExists
import Util.isMac
import Util.isMultiplatform
import Util.isWindows
import Util.testShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.com.intellij.util.io.Murmur3_32Hash
import org.jetbrains.kotlin.daemon.common.toHexString
import org.jetbrains.kotlin.utils.addToStdlib.partitionIsInstance
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.ImageType.DEB
import org.panteleyev.jpackage.ImageType.DMG
import org.panteleyev.jpackage.ImageType.EXE
import org.panteleyev.jpackage.ImageType.MSI
import org.panteleyev.jpackage.ImageType.PKG
import org.panteleyev.jpackage.ImageType.RPM
import org.panteleyev.jpackage.JPackageTask
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

plugins {
    application
    alias(libs.plugins.jpackage)
    alias(libs.plugins.shadowJar)
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

// Shadow Jar
tasks.withType<ShadowJar> {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to "Alchemist",
                "Implementation-Version" to rootProject.version,
                "Main-Class" to "it.unibo.alchemist.Alchemist",
                "Automatic-Module-Name" to "it.unibo.alchemist",
            ),
        )
    }
    exclude(
        "ant_tasks/",
        "about_files/",
        "help/about/",
        "build",
        ".gradle",
        "build.gradle",
        "gradle",
        "gradlew.bat",
        "gradlew",
    )
    isZip64 = true
    mergeServiceFiles()
    destinationDirectory.set(rootProject.layout.buildDirectory.map { it.dir("shadow") })
    // Run the jar and check the output
    val minJavaVersion: String by properties
    val javaExecutable = javaToolchains
        .launcherFor { languageVersion.set(JavaLanguageVersion.of(minJavaVersion)) }
        .map { it.executablePath.asFile.absolutePath }
    val testShadowJar = testShadowJar(javaExecutable, archiveFile)
    testShadowJar.configure {
        dependsOn(this@withType)
    }
    this.finalizedBy(testShadowJar)
    tasks.assemble.configure { dependsOn(testShadowJar) }
}

// Disable distTar and distZip
val toDisable = listOf(
    tasks.distTar,
    tasks.distZip,
    tasks.jpackage,
    tasks.shadowDistZip,
    tasks.shadowDistTar,
).map { it.name }
tasks.matching { it.name in toDisable }.configureEach { enabled = false }

sealed interface PackagingMethod
data class ValidPackaging(val format: ImageType, val perUser: Boolean = false) : PackagingMethod {
    val name get() = format.formatName

    override fun toString() = "$name packaging${ " (userspace)".takeIf { perUser }.orEmpty() }"
}
data class DisabledPackaging(val reason: String) : PackagingMethod

val ImageType.formatName get() = name.lowercase()
fun ImageType.disabledBecause(reason: String): List<DisabledPackaging> = listOf(
    DisabledPackaging("$formatName packaging disabled because $reason"),
)
fun ImageType.disabledOnNon(os: String): List<DisabledPackaging> = disabledBecause("unsupported non non-$os systems")
fun ImageType.valid(): List<ValidPackaging> = listOf(ValidPackaging(this)) + when (this) {
    MSI, EXE -> listOf(ValidPackaging(this, true))
    else -> emptyList()
}
fun ImageType.validIfCommandExists(command: String): List<PackagingMethod> = when {
    commandExists(command) -> valid()
    else -> disabledBecause("the required command '$command' could not be found in PATH.")
}

val packageRequirements: List<PackagingMethod> = ImageType.values().flatMap { format ->
    when (format) {
        EXE, MSI -> if (isWindows) format.valid() else format.disabledOnNon("Windows")
        DMG, PKG -> if (isMac) format.valid() else format.disabledOnNon("MacOS")
        DEB -> when {
            isWindows || isMac -> format.disabledOnNon("Linux")
            else -> format.validIfCommandExists("dpkg")
        }
        RPM -> when {
            isWindows || isMac -> format.disabledOnNon("Linux")
            else -> format.validIfCommandExists("rpmbuild")
        }
        else -> format.disabledBecause("it is currently not supported. If needed, open a feature request.")
    }
}

val (validFormats, disabledFormats) = packageRequirements.partitionIsInstance<PackagingMethod, ValidPackaging>()

disabledFormats.filterIsInstance<DisabledPackaging>().forEach { logger.warn(it.reason) }

val versionComponentExtractor = Regex("^(\\d+\\.\\d+\\.)(\\d+)(.*)$")
private data class SemVerExtracted(val base: String, val patch: String, val suffix: String) {
    fun asMangledVersion(): String = "${base}0${patch}0${
        if (suffix.isEmpty()) {
            ""
        } else {
            val asBytes = suffix.toByteArray(StandardCharsets.UTF_8)
            Murmur3_32Hash.MURMUR3_32.hashBytes(asBytes, 0, asBytes.size).toUInt().toString()
        }
    }"
}
private fun String.extractVersionComponents(): SemVerExtracted {
    val (base, patch, suffix) = versionComponentExtractor.matchEntire(this)
        ?.destructured
        ?: error("Invalid version format: $this")
    return SemVerExtracted(base, patch, suffix)
}

validFormats.forEach { packaging: ValidPackaging ->
    val baseVersion: Provider<String> = provider { rootProject.version.toString() }
    val packageSpecificVersion = baseVersion.map { version ->
        when (packaging.format) {
            MSI, EXE -> version.substringBefore('-')
            DMG, PKG -> version.extractVersionComponents().asMangledVersion()
            RPM -> version.replace('-', '.')
            else -> version
        }
    }
    val packagingTaskNameSuffix = "${packaging.name.capitalized()}${"PerUser".takeIf { packaging.perUser }.orEmpty()}"
    val packagingTask = tasks.register<JPackageTask>("jpackage$packagingTaskNameSuffix") {
        dependsOn(tasks.shadowJar)

        group = "Distribution"
        description = "Creates application bundle through jpackage using $packaging"
        // General info
        resourceDir = "$projectDir/package-settings"
        appName = rootProject.name
        appVersion = packageSpecificVersion.get()
        copyright = "Danilo Pianini and the Alchemist contributors"
        aboutUrl = "https://alchemistsimulator.github.io/"
        appDescription = rootProject.description
        licenseFile = "${rootProject.projectDir}/LICENSE.md"

        type = packaging.format
        input = tasks.shadowJar.get().archiveFile.get().asFile.parent
        // Packaging settings
        destination = rootProject.layout.buildDirectory.map { it.dir("package") }.get().asFile.path
        mainJar = tasks.shadowJar.get().archiveFileName.get()
        mainClass = application.mainClass.get()
        verbose = true

        linux {
            icon = "${project.projectDir}/package-settings/logo.png"
            linuxShortcut = true
            linuxDebMaintainer = "Danilo Pianini"
            linuxRpmLicenseType = "GPLv3"
        }
        windows {
            icon = "${project.projectDir}/package-settings/logo.ico"
            winDirChooser = true
            winPerUserInstall = packaging.perUser
            winShortcutPrompt = true
            winConsole = true
        }
        mac {
            icon = "${project.projectDir}/package-settings/logo.png"
        }
    }
    tasks.assemble.configure { dependsOn(packagingTask) }
    // AUR Package based on the RPM distribution
    if (packaging.format == RPM) {
        logger.info("RPM packaging supported, enabling PKGBUILD support as well")
        val generatePKGBUILD by tasks.registering {
            group = "Distribution"
            description = "Generates a valid PKGBUILD by replacing values in the template file"
            dependsOn(packagingTask)
            inputs.files(packagingTask)
            doLast {
                val inputFile = file("${project.projectDir}/PKGBUILD.template")
                val template = inputFile.readText()
                val templateStrings = listOf("VERSION", "RPM_URL", "RPM_MD5").map { "%ALCHEMIST_$it%" }
                templateStrings.forEach {
                    check(it in template) { "Corrupt PKGBUILD.template, missing $it" }
                }
                val outputDir = rootProject.layout.buildDirectory.map { it.dir("pkgbuild") }.get().asFile
                if (!outputDir.mkdirs()) {
                    check(outputDir.exists() && outputDir.isDirectory) {
                        "Could not create output directory $outputDir, as it already exists and is not a directory"
                    }
                }
                val rpmFileName = "${rootProject.name}-${packageSpecificVersion.get()}-1.x86_64.rpm"
                val rpmFile = packagingTask.map { File(it.destination).resolve(rpmFileName) }.get()
                check(rpmFile.exists()) { "Could not find $rpmFileName in ${rpmFile.parentFile}" }
                check(rpmFile.isFile) { "$rpmFileName is a directory" }
                val md5 = MessageDigest.getInstance("MD5")
                rpmFile.inputStream().use {
                    while (it.available() > 0) {
                        md5.update(it.readNBytes(10 * 1024 * 1024))
                    }
                }
                val replacements = templateStrings.associateWith { key ->
                    when {
                        "VERSION" in key -> baseVersion.get().replace('-', '.')
                        "RPM_URL" in key ->
                            "https://github.com/AlchemistSimulator/Alchemist/releases/download/" +
                                "${baseVersion.get()}/" +
                                // Replace x86_64 with $CARCH to avoid namcap warnings
                                rpmFileName.replace("x86_64", "\$CARCH")
                        "RPM_MD5" in key -> md5.digest().toHexString()
                        else -> error("Unknown PKGBUILD replacement key $key")
                    }
                }
                val pkgbuildContent = replacements.toList().fold(template) { base, replacement ->
                    val (key, actualValue) = replacement
                    base.replace(key, actualValue)
                }
                outputDir.resolve("PKGBUILD").writeText(pkgbuildContent)
            }
        }
        tasks.assemble.configure { dependsOn(generatePKGBUILD) }
    }
//    tasks.register<Exec>("test${packagingTaskNameSuffix}PackageInstallation") {
//        group = "Verification"
//        description = "Tries to install locally the Alchemist installer created with $packaging"
//        workingDir = rootProject.layout.buildDirectory.dir("package-${packaging.name}-install").get().asFile
//        dependsOn(packagingTask)
//        inputs.files(packagingTask)
//        doLast {
//            when (packaging.format) {
//                MSI -> commandLine(
//                    "msiexec",
//                    "-i",
//                    "${rootProject.name}-$actualVersion.msi",
//                    "-quiet", "INSTALLDIR=${workingDir.path}")
//                PKG -> commandLine(
//                    "sudo",
//                    "installer",
//                    "-pkg",
//                    "${rootProject.name}-$actualVersion.pkg",
//                    "-target",
//                    "/")
//                else -> logger.warn("No testing in place yet for $packaging")
//            }
//        }
//    }
}

// val deleteJpackageOutput by tasks.registering(Delete::class) {
//    setDelete(project.file("build/package/install"))
// }
//
// tasks.register<Exec>("testJpackageInstall") {
//    isIgnoreExitValue = true
//    workingDir = rootProject.file("build/package/")
//    doFirst {
//        val version = rootProject.version.toString().substringBefore('-')
//        // Extract the packet
//        when {
//            isWindows -> Unit
//            isMac -> commandLine("sudo", "installer", "-pkg", "${rootProject.name}-$version.pkg", "-target", "/")
//            else -> {
//                workingDir.resolve("install").mkdirs()
//                commandLine("bsdtar", "-xf", "${rootProject.name}-$version-1.x86_64.rpm", "-C", "install")
//            }
//        }
//    }
//    doLast {
//        // Check if package contains every file needed
//        var execFiles: List<String>
//        var appFiles: List<String>
//        when {
//            isWindows -> {
//                execFiles = workingDir.resolve("install").listFiles().map { it.name }
//                appFiles = workingDir.resolve("install/app").listFiles().map { it.name }
//            }
//            isMac -> {
//                val root = File("/Applications/${rootProject.name}.app")
//                execFiles = root.resolve("Contents/MacOS").listFiles().map { it.name }
//                appFiles = root.resolve("Contents/app").listFiles().map { it.name }
//            }
//            else -> {
//                execFiles = workingDir.resolve("install/opt/alchemist/bin").listFiles().map { it.name }
//                appFiles = workingDir.resolve("install/opt/alchemist/lib/app").listFiles().map { it.name }
//            }
//        }
//        require(rootProject.name in execFiles || "${rootProject.name}.exe" in execFiles)
//        require(jpackageFull.get().mainJar in appFiles)
//    }
//    mustRunAfter(jpackageFull)
//    finalizedBy(deleteJpackageOutput)
// }
//

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
