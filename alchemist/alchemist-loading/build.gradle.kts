/*
 * Copyright (C) 2010-2019) Danilo Pianini and contributors listed in the main project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist) and is distributed under the terms of the
 * GNU General Public License) with a linking exception)
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api(project(":alchemist-implementationbase"))
    api(project(":alchemist-interfaces"))
    implementation(project(":alchemist-time"))
    implementation(project(":alchemist-maps"))
    implementation(Libs.commons_lang3)
    implementation(Libs.groovy)
    implementation(Libs.guava)
    implementation(Libs.jirf)
    implementation(Libs.snakeyaml)

    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-maps"))
    testImplementation(Libs.gson)
    testImplementation(Libs.kotlintest_runner_junit5)

    testRuntimeOnly(project(":alchemist-incarnation-sapere"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "2g"
}
