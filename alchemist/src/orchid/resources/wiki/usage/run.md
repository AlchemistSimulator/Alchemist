---

title: "Running Alchemist simulations"

---

Alchemist expects a YAML map as input, such maps are contained in *.yml files and are used to write simulations. Informations about how to write alchemist simulations can be found in the next section.

## Running a simulation

The typical way of running alchemist simulations is by directly using alchemist <kbd>main</kbd> method. If you have cloned the Alchemist repository, you can also run a simulation using the produced artifacts (namely, a .jar file). The first approach is recommended.

**Running a simulation using Alchemist main**

Alchemist main class (i.e. the class which contains the <kbd>main</kbd> method) is the following: <kbd>it.unibo.alchemist.Alchemist</kbd>. In order to run a simulation, you can call its main method passing proper parameters (passing '-h' option will print out the supported options, to run a simulation you can rely on the <kbd>-y</kbd> option followed by the path to your own *.yml file).
For instance, if you have imported Alchemist as a dependency in your project, you can define a gradle task responsible for the execution of your simulation. A typical way to define such task would be the following:

```bash
tasks.register<JavaExec>("execAlchemist") {
    classpath = project.sourceSets.getByName("main").runtimeClasspath
    main = "it.unibo.alchemist.Alchemist"
    args = listOf("-y mysimulation.yml")
}
```

If you want to run a simulation *inside* your code, you can do so by  calling <kbd>it.unibo.alchemist.Alchemist.main(args)</kbd>. Note that this will cause the opening of the gui. If you want to run a simulation and manage it inside your code (or inside a gradle task) you can do so by using the YamlLoader class.

**Running a simulation using a .jar file**

In order to run a simulation, you must first generate a .jar file. To do so, you can rely on the "fatJar" gradle task. To run the task, point a terminal on the alchemist project's root and issue

```bash
./gradlew fatJar
```

As a result, an <kbd>alchemist-redist-{version}.jar</kbd> file will be generated in <kbd>build/libs/</kbd>. To launch the simulator all you have to do is executing that jar, you can do so by issuing

```bash
java -jar path/to/jar/alchemist-redist-{version}.jar {options}
```

Alchemist supports multiple options, run the program with the <kbd>-h</kbd> (help) otpion in order to get a list of all of them. To run a simulation you can rely on the <kbd>-y</kbd> option and pass your own *.yml file. A typical command to launch a simulation would be:

```bash
java -jar path/to/jar/alchemist-redist-{version}.jar -y mysimulation.yml
```