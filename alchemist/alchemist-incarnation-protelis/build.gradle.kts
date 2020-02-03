/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-interfaces"))
    api(project(":alchemist-implementationbase"))
    api(Libs.protelis_interpreter) {
        /*
         * TODO: once Protelis fixes its dependency issues, remove this one.
         * Check with:
         * ./gradlew dependencyInsight --dependency org.eclipse.emf:org.eclipse.emf.ecore --configuration runtimeClasspath
         */
        implementation(Libs.org_eclipse_emf_ecore) {
            isForce = true
        }
    }
    api(Libs.protelis_lang)
    implementation(project(":alchemist-time"))
    implementation(project(":alchemist-maps"))
    //implementation(files("/home/giacomo/eclipse-workspace/ns3asy-bindings/build/libs/ns3asy-bindings-all-0.1.jar"))
    implementation("com.github.gscaparrotti:ns3asy-bindings:develop-SNAPSHOT");
    implementation(Libs.commons_lang3)
    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation(Libs.commons_io)
}
