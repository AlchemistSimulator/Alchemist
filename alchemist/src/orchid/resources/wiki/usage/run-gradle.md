---

title: Run Alchemist via Gradle

---
In this section you will be given information about how to use the simulator via [Gradle](https://gradle.org). If you already practiced with the [quick start](quickstart.md) you may find this information repetitive.

## Adding Alchemist dependency

First of all, you need to add Alchemist dependency to your Gradle build. You can do so by writing:
```kotlin
dependencies {
    implementation("it.unibo.alchemist:alchemist:ALCHEMIST_VERSION")
}
```
substituting `ALCHEMIST_VERSION` with the version you want to use (change the scope appropriately if you need Alchemist only for runtime or testing).

## Running Alchemist

One easy way to run Alchemist via Gradle is by writing a simple Gradle task responsible for launching the simulator. Let's see how this can be done. Firstly, [Gradle](https://gradle.org) has a special task to run a Java class from the build script: `JavaExec`. We can create our custom task of type `JavaExec` and configure it to launch Alchemist. Let's define our task:
```kotlin
tasks.register<JavaExec>("runAlchemist") {
    // run alchemist
}
```
Now, in order to launch the simulator, we need to explicit two things:
- the Alchemist main class, which is `it.unibo.alchemist.Alchemist`
- the classpath, or java won't be able to find all the classes needed

You can do so by adding two lines of code:
```kotlin
tasks.register<JavaExec>("runAlchemist") {
    classpath = project.sourceSets.getByName("main").runtimeClasspath
    main = "it.unibo.alchemist.Alchemist"
}
```
This is sufficient to succesfully run the simulator. Open a terminal and move to the project root folder, then on UNIX:
```bash
./gradlew runAlchemist
```
On Windows:
```
gradlew.bat runAlchemist
```

Note that the first launch will be rather slow, since Gradle will download all the required files. They will get cached in the user's home folder (as per Gradle normal behavior).

### Running with options

Now, to make the simulator do something for you, you should run it with proper arguments. For further information about the supported options see the [command line interface](quickstart.md#command-line-interface) (or run the simulator with the `-h` or `--help` option). The most common case is you want to run your own simulation. To do so, you can rely on the `-y` option followed by the path to the simulation file. Alchemist simulations are contained in *.yml files, more information about how to write such simulations can be found [here](yaml.md). Let's say you want to run Alchemist with the following arguments:
```bash
-y path/to/your/simulation.yml
```
You can do so in a couple of way, of course this applies to every option you may want to launch the simulator with. You can explicit such options via command line when you run your custom task, using the `--args` option. So you will have something like this:
```bash
./gradlew runAlchemist --args='-y path/to/your/simulation.yml'
```
In addition, if your options never change, you can explicit them in the body of your custom task, by adding the following line of code:

```kotlin
args = listOf("-y", "path/to/your/simulation.yml")
```

Ok, that's it. You should be able to use Alchemist via Gradle in your own project now, or at least have a clue.