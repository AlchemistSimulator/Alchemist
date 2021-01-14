---

title: Running Alchemist via Gradle

---
In this section you will be given information about how to use the simulator via [Gradle](https://gradle.org).

To ease your life,
we prepared [here](https://github.com/AlchemistSimulator/alchemist-primer) a project showing how to use Alchemist via
[Gradle](https://gradle.org) to run a simple simulation.
Let's use it as a quick start. 

## How to launch

To run the example you can rely on the pre-configured Gradle build script. It will automatically download all the required libraries, set up the environment, and execute the simulator via command line for you.
As first step, use `git` to locally clone the repository linked above.
In order to launch, open a terminal and move to the project root folder, then on UNIX:
```bash
./gradlew runAll
```
On Windows:
```
gradlew.bat runAll
```

Press P and you should see things changing.
For further information about the gui, see the [this page](default-gui.md).

## Project organization

You will see a `src/main` folder,
with subfolders divided by language.
Simulations are in the `yaml` folder.
Take a look: the files should be rather human-readable, although they will be clearer as we proceed.

Now look at the `build.gradle.kts` script,
there is some magic deserving an explanation there.

{% gistit
    repository = "alchemist-primer"
    file = "build.gradle.kts"
%}

First of all, we need to add Alchemist as a dependency.
Alchemist is available on Maven Central (and on Bintray, which mirrors it),
we can import the base components by importing the `it.unibo.alchemist:alchemist:SOME_ALCHEMIST_VERSION` artifact
(with `SOME_ALCHEMIST_VERSION` replaced by the version used).
However, to simulate anything useful we also need at least one incarnation,
and probably we also want to import other capabilities, such as visualization.

So we got:

{% gistit
    repository = "alchemist-primer"
    file = "build.gradle.kts"
    slice = "8:11"
%}

{% gistit
    repository = "alchemist-primer"
    file = "build.gradle.kts"
    slice = "21:27"
%}

If you see a `+`, it's a Gradle shorthand for "the latest version you can find".
If you see a `_`, we are using a [Gradle plugin](https://github.com/jmfayard/refreshVersions/) which delegates the version choice to [an external file](https://github.com/AlchemistSimulator/alchemist-primer/blob/master/versions.properties).

Modules add capabilities to the simulator, and are documented in the remainder of the guide.

## Running Alchemist

When Gradle manages our dependencies,
the easiest way to run the simulator is writing a simple Gradle task launching it.
Let's see how this can be done.
Firstly, Gradle has a special task to run a Java class from the build script: `JavaExec`.
We can create our custom task of type `JavaExec` and configure it to launch Alchemist.
Let's define our own:
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

Note that the first launch will be rather slow, since Gradle will download all the required files.
They will get cached in the user's home folder (as per Gradle normal behavior),
so subsequent executions will be way more agile.

The task we configure in the primer is actually more complex.

We first create a name for all the Alchemist-related tasks.
They will be pretty printed when the user runs `./gradlew tasks`
{% gistit
    repository = "alchemist-primer"
    file = "build.gradle.kts"
    slice = "31"
%}
We configure a "master" task, that launches all other run tasks in sequence:
{% gistit
    repository = "alchemist-primer"
    file = "build.gradle.kts"
    slice = "32:39"
%}
Then we generate one task per `yml` file inside `src/main/yaml`.
Please look at the code comments to understand what they do,
but the core is just to tell Gradle which class to execute and which CLI options should be passed down.
{% gistit
    repository = "alchemist-primer"
    file = "build.gradle.kts"
    slice = "39:0"
%}


### Using the Command Line Interface

To make the simulator do something for you (for instance, to make it run your own simulation) you can rely on the [command line interface](quickstart.md#command-line-interface). The most common case is you want to run your own simulation. To do so, you can rely on the `-y` option followed by the path to the simulation file. Alchemist simulations are contained in *.yml files, more information about how to write such simulations can be found [here](yaml.md). Let's say you want to run Alchemist with the following arguments:
```bash
-y path/to/your/simulation.yml
```
You can do so in a couple of ways, of course this applies to every option you may want to launch the simulator with. You can explicit such options via command line when you run your custom task, using the `--args` option. So you will have something like this:
```bash
./gradlew runAlchemist --args='-y path/to/your/simulation.yml'
```
Otherwise, if your options never change, you can explicit them in the body of your custom task, by adding the following line of code:

```kotlin
args = listOf("-y", "path/to/your/simulation.yml")
```

The command line interface features several options, related to running simulations,
deciding how often and where to export data,
how to display the information,
and so on.
A list of the commands can be printed by passing `--help` as argument.

## Importing the project into an IDE

Alchemist projects can be imported in several IDEs:
as far as the development environment supports Gradle,
then it supports development of Alchemist projects.
The IDE we recommend for development is IntelliJ Idea,
which (among those we tested out) has the better integration with Gradle.
Importing the project in Idea is trivial:
just open the folder containing the `build.gradle.kts` file with the IDE.
Depending on the version and the IDE settings,
it could import the project straight away or ask which import format to prefer:
pick "Gradle".

Once the project is ready, it can be developed as any other project.
IntelliJ provides a syntax helper for YAML which is rather handy.
Since the IDE can run gradle tasks, it is also possible to launch the simulator directly from the IDE.
