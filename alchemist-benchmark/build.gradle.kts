import Libs.alchemist

plugins {
    application
}

group = "it.unibo.alchemist"
version = "0.1.0-archeo+68647cc14"

repositories {
    mavenCentral()
}

sourceSets {
    main {
        resources {
            srcDir("src/main/protelis")
        }
    }
}

dependencies {
    implementation(alchemist("api"))
    implementation(alchemist("engine"))
    implementation(alchemist("loading"))
    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))
    implementation(alchemist("incarnation-protelis"))
    implementation(project(mapOf("path" to ":")))
    runtimeOnly(rootProject)
}

application {
    mainClass.set("it.unibo.alchemist.Alchemist")
}
