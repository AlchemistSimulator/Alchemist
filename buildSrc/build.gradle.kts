plugins {
    `kotlin-dsl`
    id("org.danilopianini.gradle-kotlin-qa") version "0.24.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

with(extensions.getByType<VersionCatalogsExtension>().named("libs")) {
    dependencies {
        implementation(findLibrary("gson").get())
    }
}
