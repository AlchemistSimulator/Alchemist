import Libs.alchemist

plugins {
    id("kotlin-multiplatform-convention")
}
kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(alchemist("api"))
                implementation(libs.ksp.api)
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.compile)
                runtimeOnly(libs.bundles.testing.runtimeOnly)
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}
