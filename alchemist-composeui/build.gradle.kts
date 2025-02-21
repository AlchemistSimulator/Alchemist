plugins {
    id("kotlin-multiplatform-convention")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val webModuleName = "alchemist-composeui"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.components.resources)
            }
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        developers {
            developer {
                name.set("Tommaso Bailetti")
                email.set("tommaso.bailetti@studio.unibo.it")
            }
        }
    }
}

tasks.wasmJsProductionExecutableCompileSync {
    dependsOn(tasks.jsBrowserProductionWebpack)
}
