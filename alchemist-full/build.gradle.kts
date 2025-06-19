/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
import Libs.alchemist
import Util.commandExists
import Util.isMac
import Util.isWindows
import Util.testShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.jetbrains.kotlin.daemon.common.toHexString
import org.panteleyev.jpackage.ImageType
import org.panteleyev.jpackage.ImageType.DEB
import org.panteleyev.jpackage.ImageType.DMG
import org.panteleyev.jpackage.ImageType.EXE
import org.panteleyev.jpackage.ImageType.MSI
import org.panteleyev.jpackage.ImageType.PKG
import org.panteleyev.jpackage.ImageType.RPM
import org.panteleyev.jpackage.JPackageTask

plugins {
    id("kotlin-jvm-convention")
    application
    alias(libs.plugins.jpackage)
    alias(libs.plugins.shadowJar)
}

buildscript {
    dependencies {
        classpath(libs.guava)
    }
}

dependencies {
    runtimeOnly(rootProject)
    rootProject.subprojects.filterNot { it == project }.forEach {
        runtimeOnly(it)
        dokka(it)
    }
    testImplementation(rootProject.libs.slf4j)
    testImplementation(rootProject)
    testImplementation(alchemist("loading"))
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
    val javaExecutable =
        javaToolchains
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
val toDisable =
    listOf(
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

fun ImageType.valid(): List<ValidPackaging> = listOf(ValidPackaging(this))

fun ImageType.validIfCommandExists(command: String): List<PackagingMethod> = when {
    commandExists(command) -> valid()
    else -> disabledBecause("the required command '$command' could not be found in PATH.")
}

val packageRequirements: List<PackagingMethod> =
    ImageType.values().flatMap { format ->
        when (format) {
            EXE, MSI -> if (isWindows) format.valid() else format.disabledOnNon("Windows")
            DMG, PKG -> if (isMac) format.valid() else format.disabledOnNon("MacOS")
            DEB ->
                when {
                    isWindows || isMac -> format.disabledOnNon("Linux")
                    else -> format.validIfCommandExists("dpkg")
                }
            RPM ->
                when {
                    isWindows || isMac -> format.disabledOnNon("Linux")
                    else -> format.validIfCommandExists("rpmbuild")
                }
            else -> format.disabledBecause("it is currently not supported. If needed, open a feature request.")
        }
    }

val (validFormats, disabledFormats) = packageRequirements.partition { it is ValidPackaging }

disabledFormats.filterIsInstance<DisabledPackaging>().forEach { logger.warn(it.reason) }

val versionComponentExtractor = Regex("^(\\d+\\.\\d+\\.)(\\d+)(.*)$")

private data class SemVerExtracted(val base: String, val patch: String, val suffix: String) {
    fun asMangledVersion(): String = "${base}0${patch}0${
        when {
            suffix.isBlank() -> ""
            else -> {
                val asBytes = suffix.toByteArray(StandardCharsets.UTF_8)
                Hashing.murmur3_32_fixed().hashBytes(asBytes).padToLong().toUInt().toString()
            }
        }
    }"
}

private fun String.extractVersionComponents(): SemVerExtracted {
    val (base, patch, suffix) =
        versionComponentExtractor
            .matchEntire(this)
            ?.destructured
            ?: error("Invalid version format: $this")
    return SemVerExtracted(base, patch, suffix)
}

private val packageDestinationDir = rootProject.layout.buildDirectory.map { it.dir("package") }.get().asFile
private val baseVersion: Provider<String> = provider { rootProject.version.toString() }
private fun ImageType.formatVersion(version: String): String = when (this) {
    MSI, EXE -> version.substringBefore('-')
    DMG, PKG -> version.extractVersionComponents().asMangledVersion()
    RPM -> version.replace('-', '.')
    else -> version
}
private val rpmFileName = baseVersion.map { "${rootProject.name}-${RPM.formatVersion(it)}-1.x86_64.rpm" }
private val rpmFileProvider = rpmFileName.map { packageDestinationDir.resolve(it) }

val generatePKGBUILD by tasks.registering {
    group = "Distribution"
    description = "Generates a valid PKGBUILD by replacing values in the template file"
    enabled = false
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
        val rpmFile = rpmFileProvider.get()
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
                        rpmFileName.get().replace("x86_64", "\$CARCH")
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

val packageTasks = validFormats.filterIsInstance<ValidPackaging>().map { packaging: ValidPackaging ->
    val packageSpecificVersion = baseVersion.map { packaging.format.formatVersion(it) }
    val packagingTaskNameSuffix = packaging.name.replaceFirstChar { it.titlecase() } +
        "PerUser".takeIf { packaging.perUser }.orEmpty()
    val rpmFile = rpmFileName.map { packageDestinationDir.resolve(it) }
    tasks.register<JPackageTask>("jpackage$packagingTaskNameSuffix") {
        group = "Distribution"
        description = "Creates application bundle through jpackage using $packaging"
        // Dependencies
        dependsOn(tasks.shadowJar)
        tasks.assemble.configure { dependsOn(this@register) }
        generatePKGBUILD.configure { dependsOn(this@register) }
        if (packaging.format == RPM) {
            logger.info("RPM packaging supported, enabling PKGBUILD support as well")
            generatePKGBUILD.configure { enabled = true }
        }
        // General info
        resourceDir = projectDir.resolve("package-settings")
        appName = rootProject.name
        appVersion = packageSpecificVersion.get()
        copyright = "Danilo Pianini and the Alchemist contributors"
        aboutUrl = "https://alchemistsimulator.github.io/"
        appDescription = rootProject.description
        licenseFile = rootProject.projectDir.resolve("LICENSE.md")
        type = packaging.format
        input = tasks.shadowJar.get().archiveFile.get().asFile.parentFile
        // Packaging settings
        destination = packageDestinationDir
        mainJar = tasks.shadowJar.get().archiveFileName.get()
        mainClass = application.mainClass.get()
        verbose = true
        linux {
            icon = project.projectDir.resolve("package-settings/logo.png")
            linuxShortcut = true
            linuxDebMaintainer = "Danilo Pianini"
            linuxRpmLicenseType = "GPLv3"
        }
        windows {
            icon = project.projectDir.resolve("package-settings/logo.ico")
            winDirChooser = true
            winPerUserInstall = packaging.perUser
            winShortcutPrompt = true
            winConsole = true
        }
        mac {
            icon = project.projectDir.resolve("package-settings/logo.png")
        }
    }
}

tasks.withType<AbstractArchiveTask> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

publishing.publications {
    withType<MavenPublication>().configureEach {
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

afterEvaluate {
    components.withType<AdhocComponentWithVariants>().named("java").configure {
        // Shadow creates a "shadowRuntimeElements" configuration which is added as a variant
        withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
            skip()
        }
    }
}
