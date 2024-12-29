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
        val jvmMain by getting {
            dependencies {
                implementation(alchemist("api"))
            }
        }
    }
}

rootProject.tasks.named("kotlinStoreYarnLock").configure {
    dependsOn(rootProject.tasks.named("kotlinUpgradeYarnLock"))
}
