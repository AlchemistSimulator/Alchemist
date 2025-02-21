import Libs.alchemist
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinTargetWithNodeJsDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsBinaryContainer
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig



plugins {
    kotlin("multiplatform")
    id("dokka-convention")
    id("power-assert-convention")
    id("static-analysis-convention")
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all") // Enable default methods in Kt interfaces
        }
    }

    fun KotlinJsTargetDsl.devServer() {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf())
                }
            }
        }
    }

    fun KotlinJsTargetDsl.webCommonConfiguration() {
        moduleName = project.name
        browser {
            commonWebpackConfig {
                outputFileName = "$moduleName.js"
            }
        }
        binaries.executable()
    }

    js {
        webCommonConfiguration()
        useEsModules()
    }

    wasmJs {
        webCommonConfiguration()
        devServer()
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                val kotlinTest by catalog
                val kotestAssertionsCore by catalog
                val kotestFrameworkEngine by catalog
                implementation(kotlinTest)
                implementation(kotestAssertionsCore)
                implementation(kotestFrameworkEngine)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(alchemist("api"))
            }
        }
        val jvmTest by getting {
            dependencies {
                val `kotest-runner` by catalog
                implementation(`kotest-runner`)
            }
        }
    }
}
