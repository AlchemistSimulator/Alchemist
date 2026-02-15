import Libs.alchemist

plugins {
    id("kotlin-multiplatform-convention")
}

kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(alchemist("api"))
                implementation(libs.ksp.api)
            }
        }
    }
}
