import Libs.alchemist
import Libs.incarnation

dependencies {
    api(alchemist("interfaces"))

    implementation(alchemist("euclidean-geometry"))
    implementation(alchemist("implementationbase"))

    testImplementation(incarnation("protelis"))
    testImplementation(alchemist("loading"))
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Federico Pettinari")
                    email.set("federico.pettinari2@studio.unibo.it")
                }
            }
        }
    }
}
