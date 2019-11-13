---

title: Running Alchemist stand-alone

---
Another way of running Alchemist is by using the redistributable jar file. However, using the simulator via [Gradle](https://gradle.org) is recommended. For further information, see how to [Run Alchemist via Gradle](run-gradle.md).

Such jar file can be downloaded from the [releases section on github](https://github.com/AlchemistSimulator/Alchemist/releases).

## Running Alchemist

If you got the redistributable, runnable Alchemist jar file, launching the simulator is straightforward. Open a terminal and move to the folder where the jar is located, then issue:
```bash
java -jar alchemist-redist-VERSION.jar
```
Remember to substitute `VERSION` with the Alchemist version you actually have downloaded.

### Using the Command Line Interface

To make the simulator do something for you (for instance, to make it run your own simulation) you can rely on the [command line interface](quickstart.md#command-line-interface). Try to run the simulator with `-h` or `--help` option in order to get a list of the supported options:
```bash
java -jar alchemist-redist-VERSION.jar -h
```

The most common case is you want to run your own simulation. To do so, you can rely on the `-y` option followed by the path to the simulation file. Alchemist simulations are contained in *.yml files, more information about how to write such simulations can be found [here](yaml.md). So a typical command would be:
```bash
java -jar alchemist-redist-VERSION.jar -y path/to/your/simulation.yml
```

## How to export the jar

Alchemist executable jar files can be generated at need, let's see how. As first step, use git to locally clone the [Alchemist repository](https://github.com/AlchemistSimulator/Alchemist). Then, to generate the jar file you can rely on the `fatJar` gradle task. To run the task, point a terminal on the alchemist project's root, then on UNIX:
```bash
./gradlew fatJar
```
On Windows:
```
gradlew.bat fatJar
```

As a result, an `alchemist-redist-{version}.jar` file will be generated in `build/libs/`. You can use it as described above.