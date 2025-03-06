import Libs.alchemist
import Util.webCommonConfiguration
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("common-static-analysis-convention")
    id("dokka-convention")
    id("kotlin-static-analysis-convention")
    id("power-assert-convention")
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all") // Enable default methods in Kt interfaces
        }
    }

    js {
        webCommonConfiguration()
    }

    sourceSets {
        val alchemistApi = alchemist("api")
        val alchemistMaintenanceTooling = alchemist("maintenance-tooling")
        val isNotRootRootProject = project != alchemistMaintenanceTooling && project != alchemistApi
        val commonMain by getting {
            dependencies {
                if (isNotRootRootProject) {
                    implementation(alchemist("maintenance-tooling"))
                }
            }
        }
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
                if (isNotRootRootProject) {
                    implementation(alchemistApi)
                }
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
