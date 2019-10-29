---

title: "Running Alchemist simulations"

---

Alchemist expects a YAML map as input, YAML maps are contained in *.yml files and are used to write simulations. Informations about how to write alchemist simulations can be found in the next section.

## Running a simulation

In order to run a simulation, you must first generate a .jar file. To do so, you can rely on the "fatJar" gradle task. To run the task, point a terminal on the project's root and issue

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