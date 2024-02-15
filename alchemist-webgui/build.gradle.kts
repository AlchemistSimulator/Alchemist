import Libs.alchemist
import Libs.incarnation
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

group = "it.unibo.alchemist"
val alchemistGroup = "Run Alchemist"
val kvisionVersion = "7.3.1"

plugins {
    application
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.graphql.server)
    alias(libs.plugins.graphql.client)

    id("io.kvision") version "7.3.1"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val task by tasks.register<JavaExec>("runWebGui") {
    group = alchemistGroup
    description = "Launches simulation in a browser web interface"

    val simulationFile = project.objects.property(String::class.java)

    simulationFile.set(project.findProperty("simulationFile") as String? ?: "")

    mainClass.set("it.unibo.alchemist.Alchemist")

    args("run", simulationFile.get())

    classpath(
        tasks.named("compileKotlinJvm"),
        configurations.named("jvmRuntimeClasspath"),
        tasks.named("jsBrowserProductionWebpack").flatMap { it.outputs.files.elements },
        tasks.named("jsBrowserDistribution").flatMap { it.outputs.files.elements },
        project(":alchemist-graphql")
            .tasks.named("generateAlchemist-graphqlApolloSources")
            .flatMap { it.outputs.files.elements },
    )
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
            runTask(
                Action {
                    mainOutputFileName = "alchemist-webgui.js"
                    sourceMaps = false
                    devServer = KotlinWebpackConfig.DevServer(
                        open = false,
                        port = 3000,
                        proxy = mutableMapOf(
                            "/kv/*" to "http://localhost:8080",
                            "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true),
                        ),
                        static = mutableListOf("${layout.buildDirectory.asFile.get()}/processedResources/js/main"),
                    )
                },
            )

            webpackTask(
                Action {
                    mainOutputFileName = "alchemist-webgui.js"
                },
            )

            testTask(
                Action {
                    useKarma {
                        useChromeHeadless()
                    }
                },
            )

            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(alchemist("graphql"))
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.apollo.runtime)
                implementation(libs.kotlin.stdlib)
                implementation(libs.korim)
                implementation(libs.redux.kotlin.threadsafe)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(incarnation("sapere"))
                implementation(incarnation("protelis"))
                implementation(alchemist("cognitive-agents"))
                implementation(alchemist("full"))
                implementation("io.ktor:ktor-server-html-builder:2.3.8")
                implementation(libs.bundles.ktor.server)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(libs.kotlin.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation(libs.apollo.runtime)
                implementation(libs.bundles.ktor.client)
                implementation(libs.bundles.kotlin.react)

                implementation("io.kvision:kvision:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
                implementation("io.kvision:kvision-richtext:$kvisionVersion")
                implementation("io.kvision:kvision-tom-select:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                implementation("io.kvision:kvision-toastify:$kvisionVersion")
                implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-icons:$kvisionVersion")
                implementation("io.kvision:kvision-rest:$kvisionVersion")
                implementation("io.kvision:kvision-state:$kvisionVersion")
                implementation("io.kvision:kvision-state-flow:$kvisionVersion")
                implementation("io.kvision:kvision-redux-kotlin:$kvisionVersion")
            }
        }
    }
}

fun PatternFilterable.excludeGenerated() = exclude { "build${File.separator}generated" in it.file.absolutePath }
tasks.withType<Detekt>().configureEach { excludeGenerated() }
ktlint { filter { excludeGenerated() } }

tasks.named("runKtlintFormatOverCommonMainSourceSet").configure {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}

tasks.named("runKtlintCheckOverCommonMainSourceSet").configure {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}
