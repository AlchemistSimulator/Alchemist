import Libs.alchemist

plugins {
    id("kotlin-jvm-convention")
}

dependencies {

    api(alchemist("api"))
    implementation(libs.ksp.api)

    testImplementation(libs.bundles.testing.compile)
    testRuntimeOnly(libs.bundles.testing.runtimeOnly)
}
