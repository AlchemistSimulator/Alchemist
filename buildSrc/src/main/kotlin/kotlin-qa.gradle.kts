import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
