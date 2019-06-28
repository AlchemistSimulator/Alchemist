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
        implementation("org.eclipse.emf:org.eclipse.emf.ecore:2.12.0") {
            isForce = true
        }
    }
    api(Libs.protelis_lang)
    implementation(project(":alchemist-time"))
    implementation(project(":alchemist-maps"))
    implementation(Libs.commons_lang3)
    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testImplementation(Libs.commons_io)
}
