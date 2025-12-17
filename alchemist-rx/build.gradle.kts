import Libs.alchemist
import Libs.incarnation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-jvm-convention")
}

dependencies {
    api(alchemist("api"))
    api(alchemist("implementationbase"))
    api(alchemist("engine"))
    implementation(libs.arrow.core)
    implementation(libs.boilerplate)
    implementation(libs.guava)

    testImplementation(alchemist("euclidean-geometry"))
    testImplementation(alchemist("test"))
    testImplementation(alchemist("implementationbase"))
    testImplementation(incarnation("biochemistry"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            contributors {
                contributor {
                    name.set("Stefano Furi")
                    email.set("stefano.furi@studio.unibo.it")
                }
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(
            listOf(
                "-Xcontext-parameters",
            ),
        )
    }
}
