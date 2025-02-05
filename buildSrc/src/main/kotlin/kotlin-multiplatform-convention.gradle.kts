import Libs.alchemist
import org.jetbrains.kotlin.gradle.targets.js.ir.DefaultIncrementalSyncTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
    id("dokka-convention")
    id("power-assert-convention")
}

kotlin {
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all") // Enable default methods in Kt interfaces
        }
    }
    js {
        browser()
        nodejs()
    }
    sourceSets {
        val commonTest by getting {
            dependencies {
                val `kotest-assertions-core` by catalog
                val `kotest-framework-engine` by catalog
                implementation(`kotest-assertions-core`)
                implementation(`kotest-framework-engine`)
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
