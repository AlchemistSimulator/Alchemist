import Libs.alchemist

dependencies {
    implementation(rootProject)
    implementation(alchemist("loading"))
    with(libs.apache.commons) {
        implementation(cli)
        implementation(io)
    }
    implementation(libs.apache.commons.cli)
    implementation(libs.guava)
    implementation(libs.logback)
}
