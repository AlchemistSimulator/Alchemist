/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    implementation(project(":alchemist-implementationbase"))
    implementation(project(":alchemist-interfaces"))
    implementation(project(":alchemist-time"))
    implementation(project(":alchemist-maps"))
    implementation("com.google.guava:guava:${extra["guavaVersion"]}")
    implementation("org.codehaus.groovy:groovy:${extra["groovyVersion"]}")
    implementation("org.apache.commons:commons-lang3:${extra["lang3Version"]}")
    implementation("org.danilopianini:jirf:${extra["jirfVersion"]}")
    implementation("org.yaml:snakeyaml:${extra["snakeyamlVersion"]}")
    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-maps"))
    testImplementation("com.google.code.gson:gson:${extra["gsonVersion"]}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${extra["kotlinTestVersion"]}")
    testRuntimeOnly(project(":alchemist-incarnation-sapere"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "2g"
}
