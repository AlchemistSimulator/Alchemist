import Libs.alchemist

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
