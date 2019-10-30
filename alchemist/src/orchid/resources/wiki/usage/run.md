---

title: "Running Alchemist simulations"

---

Alchemist expects a YAML map as input, such maps are contained in *.yml files and are used to write simulations. Informations about how to write alchemist simulations can be found in the next section.

## Running a simulation

There are mainly two ways to run alchemist simulations, you can either want to run a simulation (1) in your code (for instance if you are developing your own project and have set alchemist as a dependency, or if you are writing alchemist tests), or run it (2) using the produced artifacts (namely, a .jar file).

**Running a simulation in your code**

There are mainly two ways to launch a simulation in your code: you can either call the main method of alchemist, passing proper parameters (passing '-h' option will print out the supported options, to run a simulation you can rely on the <kbd>-y</kbd> option followed by the path to your own *.yml file). To do so just call <kbd>it.unibo.alchemist.Alchemist.main(args)</kbd> in your code. Note that this will cause the opening of the gui.
The other way to run a simulation is to use the YamlLoader class, in this case the gui will not be triggered and you can start the simulation and manage it inside your code.

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
java -jar path/to/jar/libs/alchemist-redist-{version}.jar -y mysimulation.yml
```