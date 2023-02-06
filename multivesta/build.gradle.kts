import Libs.alchemist

dependencies {
    implementation(rootProject)
    implementation(alchemist("adapter"))
    implementation(files("libs/multivesta.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

/*tasks.getByName<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to "samplemvintegration.multivesta.EntryPointSampleIntegration",
        )
    }
    // add the external libs to the jar
    configurations.compileClasspath.get().forEach {
        from(project.zipTree(it))
    }
}*/

tasks.create<Exec>("runLinux") {
    dependsOn(tasks.build)
    val jarName = buildDir.absolutePath + "/libs/" + project.name + "-" + project.version + ".jar"
    commandLine(
        "java", "-jar", jarName, "-c", "-sots", "1", "-bs", "30", "-a", "0.05", "-ds", "[0.1]",
        "-l", "1", "-m", "none", "-f", "./MultiQuaTEx/avgHead.multiquatex", "-otherParams", "-pHead 0.5",
        "-ir", "1", "-sm", "false", "-sd", "samplemvintegration.multivesta.CoinSimState", "-vp", "true",
        "-mvad", "1E-7", "-wm", "2", "-pw", "6", "-nb", "128", "-ibs", "30"
    )
}

tasks.create("runMac") {
    // actually, this is the same as the linux one
    dependsOn("runLinux")
}

tasks.create("runWindows") { // not tested
    dependsOn(tasks.build)
    val jarName = buildDir.absolutePath + "/libs/" + project.name + "-" + project.version + ".jar"
    doFirst { // start server
        exec {
            commandLine("java", "-jar", jarName, 49141)
        }
    }
    doLast { // start client
        exec {
            commandLine(
                "java", "-jar", jarName, "-c", "-sots", "1", "-bs", "30", "-a", "0.05", "-ds", "[0.1]",
                "-l", "1", "-m", "none", "-f", "./MultiQuaTEx/avgHead.multiquatex", "-otherParams", "-pHead 0.5",
                "-ir", "1", "-sm", "false", "-sd", "samplemvintegration.multivesta.CoinSimState", "-vp", "true",
                "-mvad", "1E-7", "-wm", "2", "-pw", "6", "-nb", "128", "-ibs", "30"
            )
        }
    }
}
