/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main(project"s alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution"s top directory.
 */

dependencies {
    api("com.github.gscaparrotti:ns3asy-bindings:0.2.0")
    testImplementation(project(":alchemist-interfaces"))
    testImplementation(project(":alchemist-implementationbase"))
    testImplementation(project(":alchemist-engine"))
    testImplementation(project(":alchemist-loading"))
    testRuntime(project(":alchemist-incarnation-protelis"))
}

tasks.register("download", Exec::class) {
    commandLine("sh", "./ns3asy.sh")
}

tasks.getByName("download") {
    //ns3asy must be downloaded (if not present) only if we're under linux
    onlyIf {
        !(File("./alchemist-ns3/tmp/ns3/ns-allinone-3.29/ns-3.29/build/lib/libns3.29-ns3asy-debug.so").isFile)
            && System.getProperty("os.name").contains("linux", true)
    }
}

tasks.test {
    dependsOn(tasks.getByName("download"))
}

tasks.withType(Test::class) {
    environment("LD_LIBRARY_PATH", "tmp/ns3/ns-allinone-3.29/ns-3.29/build/lib")
}
