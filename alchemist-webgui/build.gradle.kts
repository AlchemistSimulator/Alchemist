import Libs.alchemist
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

group = "it.unibo.alchemist"
val alchemistGroup = "Run Alchemist"
val kvisionVersion = "7.3.1"

plugins {
    application
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graphql.server)
    alias(libs.plugins.graphql.client)

    id("io.kvision") version "7.3.1"
}

repositories {
    mavenCentral()
    mavenLocal()
}

/*dependencies {
    implementation(alchemist("graphql"))
    implementation(alchemist("api"))
    implementation(alchemist("full"))

    implementation(libs.apollo.runtime)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktor.server.websockets)
    implementation(libs.bundles.graphql.server)
    implementation(libs.bundles.ktor.server)

    implementation(libs.kotest.runner)
    implementation(libs.ktor.server.test.host)
}*/

/*application {
    mainClass.set("it.unibo.alchemist.Alchemist")
}*/

val task by tasks.register<JavaExec>("runWEB") {
    dependencies {
        implementation(alchemist("graphql"))
        implementation(alchemist("api"))
        implementation(alchemist("full"))
    }
    group = alchemistGroup
    description = "Launches simulation"

    val simulationFile = project.objects.property(String::class.java)

    simulationFile.set(project.findProperty("simulationFile") as String? ?: "C:/Users/Tiziano/Desktop/Tiziano/UNI/Test/Alchemist fork/Alchemist/test.yml")

    mainClass.set("it.unibo.alchemist.Alchemist")

    args("run", "C:/Users/Tiziano/Desktop/Tiziano/UNI/Test/yaml/" + simulationFile.get())

    doFirst {
        val libsDir = "C:/Users/Tiziano/Desktop/Tiziano/UNI/Test/Alchemist fork/Alchemist/alchemist-graphql/build/libs/"
        classpath(
            sourceSets["main"].runtimeClasspath,
            fileTree(libsDir) {
                include("**/*.jar")
                include("**/*.klib")
            },
        )
    }

    // classpath(tasks.named("compileKotlinJvm"), configurations.named("jvmRuntimeClasspath"))
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
                implementation(libs.apollo.runtime)
                implementation(libs.korim)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.redux.kotlin.threadsafe)
            }
        }

        /*val commonTest by getting {
            dependencies {
                implementation(alchemist("graphql"))
                //implementation(alchemist("api"))
                implementation(alchemist("full"))
                implementation(libs.apollo.runtime)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.ktor.server.websockets)
                implementation(libs.bundles.graphql.server)
                implementation(libs.bundles.ktor.server)

                implementation(libs.kotest.assertions)
                implementation(libs.kotest.runner)
                implementation(libs.ktor.server.test.host)
            }
        }*/

        /*val jvmMain by getting {
            dependencies {
                implementation(alchemist("graphql"))
                implementation(libs.bundles.ktor.server)
                implementation(libs.logback)
                implementation(libs.resourceloader)
                implementation(libs.apollo.runtime)
                implementation(libs.korim)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.redux.kotlin.threadsafe)
            }
        }*/

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(libs.apollo.runtime)
                implementation(libs.bundles.ktor.client)
                implementation(libs.bundles.kotlin.react)

                implementation("io.kvision:kvision:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
                implementation("io.kvision:kvision-datetime:$kvisionVersion")
                implementation("io.kvision:kvision-richtext:$kvisionVersion")
                implementation("io.kvision:kvision-tom-select:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                implementation("io.kvision:kvision-imask:$kvisionVersion")
                implementation("io.kvision:kvision-toastify:$kvisionVersion")
                implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-icons:$kvisionVersion")
                implementation("io.kvision:kvision-pace:$kvisionVersion")
                implementation("io.kvision:kvision-handlebars:$kvisionVersion")
                implementation("io.kvision:kvision-chart:$kvisionVersion")
                implementation("io.kvision:kvision-tabulator:$kvisionVersion")
                implementation("io.kvision:kvision-maps:$kvisionVersion")
                implementation("io.kvision:kvision-rest:$kvisionVersion")
                implementation("io.kvision:kvision-jquery:$kvisionVersion")
                implementation("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
                implementation("io.kvision:kvision-state:$kvisionVersion")
                implementation("io.kvision:kvision-state-flow:$kvisionVersion")
                implementation("io.kvision:kvision-ballast:$kvisionVersion")
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
