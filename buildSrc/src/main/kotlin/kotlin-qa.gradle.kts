import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}

detekt {
    allRules = true
    buildUponDefaultConfig = true
    val configPath = "$rootDir/config/detekt/detekt.yml"
    file(configPath)
        .takeIf { it.exists() }
        ?.let { config = files(it) }
        ?: logger.warn("Missing Detekt configuration at $configPath")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

// Enforce Kotlin version coherence
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin")) {
            useVersion(KOTLIN_VERSION)
            because("All Kotlin modules should use the same version, and compiler uses $KOTLIN_VERSION")
        }
    }
}
