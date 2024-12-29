import Libs.alchemist

plugins {
    kotlin("multiplatform")
    id("dokka-convention")
}

kotlin {
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all") // Enable default methods in Kt interfaces
        }
    }
    js {
        browser {
            binaries.executable()
            binaries.library()
        }
        nodejs() {
            binaries.executable()
            binaries.library()
        }
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

rootProject.tasks.named("kotlinStoreYarnLock").configure {
    dependsOn(rootProject.tasks.named("kotlinUpgradeYarnLock"))
}
