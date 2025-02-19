import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("kotlin-multiplatform-convention")
    kotlin("multiplatform")
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

    js {
        moduleName = webModuleName
        browser {
            commonWebpackConfig {
                outputFileName = "$webModuleName.js"
            }
        }
        binaries.executable()
        useEsModules()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "$webModuleName.js"
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootProject.path)
                    }
                }
            }
        }
        binaries.executable()
    }

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

        val commonWebMain by creating {
            dependsOn(commonMain)
        }

        val jsMain by getting {
            dependsOn(commonWebMain)
        }
        val wasmJsMain by getting {
            dependsOn(commonWebMain)
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

tasks {
    fun PatternFilterable.excludeGenerated() = exclude { "build${separator}generated" in it.file.absolutePath }
    withType<Detekt>().configureEach { excludeGenerated() }
    ktlint { filter { excludeGenerated() } }
    withType<Cpd> {
        dependsOn(generateComposeResClass, generateExpectResourceCollectorsForCommonMain)
        listOf("Jvm", "WasmJs", "Js").forEach { target ->
            dependsOn(named("generateActualResourceCollectorsFor${target}Main"))
        }
    }
}
