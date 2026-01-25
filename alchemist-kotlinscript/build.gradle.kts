plugins {
    id("kotlin-jvm-convention")
}

dependencies {
    implementation(kotlin("script-runtime"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
}
