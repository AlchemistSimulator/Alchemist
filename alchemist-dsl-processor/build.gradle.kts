plugins {
    id("kotlin-jvm-convention")
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.api)
    api(project(":alchemist-api"))
//    api(project(":alchemist-dsl-api"))

    testImplementation(libs.bundles.testing.compile)
    testRuntimeOnly(libs.bundles.testing.runtimeOnly)
}
