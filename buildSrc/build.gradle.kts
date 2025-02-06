plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

with(extensions.getByType<VersionCatalogsExtension>().named("libs")) {
    dependencies {
        implementation(findLibrary("dokka-gradle-plugin").get())
        implementation(findLibrary("gson").get())
        implementation(findLibrary("jgit").get())
        implementation(findLibrary("kotlin-multiplatform-plugin").get())
        implementation(findLibrary("kotlin-power-assert-plugin").get())
        implementation(findLibrary("kotlin-jvm-plugin").get())
    }
}
