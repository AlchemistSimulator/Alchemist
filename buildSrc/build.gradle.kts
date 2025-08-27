plugins {
    `kotlin-dsl`
    kotlin("jvm") version libs.versions.kotlin
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.gson)
    implementation(libs.java.quality.assurance.plugin)
    implementation(libs.jgit)
    implementation(libs.kotest.plugin)
    implementation(libs.kotlin.jvm.plugin)
    implementation(libs.kotlin.multiplatform.plugin)
    implementation(libs.kotlin.power.assert.plugin)
    implementation(libs.kotlin.quality.assurance.plugin)
    implementation(libs.ksp.plugin)
}
