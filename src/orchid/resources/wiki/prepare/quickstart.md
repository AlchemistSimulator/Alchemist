---

title: Quick start

---

You can either follow these instructions or those on the `README.md` file of the linked project, information is equivalent.

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
Press P and you will see some pedestrians wandering around.
For further information about the gui, see the {{ anchor('graphical interface shortcuts', 'Alchemist graphical interface') }}.

Note that the first launch will be rather slow, since Gradle will download all the required files.
They will get cached in the user's home folder (as per Gradle normal behavior).

## Importing the example project in an IDE

Since Gradle is used to deal with the simulator configuration,
any IDE supporting it should in principle be able to import it.
We recommend IntelliJ Idea, as to the best of our knowledge has the smoothest integration with Gradle.

If you have IntelliJ Idea, just right click from your file manager on the `build.gradle.kts` file and open it with IntelliJ.
The IDE should warn that a Gradle project has been detected,
and by confirming that this is correct it will self-configure appropriately.

### Project organization

You will see a `src/main` folder,
with subfolders divided by language.
Simulations are in the `yaml` folder.

## The build script

Let's explain how things work by looking at the.

### Importing Alchemist

First of all, we need to add Alchemist as a dependency. Alchemist is available on Maven Central, we can import all the components by importing the `it.unibo.alchemist:alchemist` artifact. Thus, you will see something like this:
```kotlin
repositories { mavenCentral() }

dependencies {
    implementation("it.unibo.alchemist:alchemist:SOME_ALCHEMIST_VERSION")
}
```
With `SOME_ALCHEMIST_VERSION` replaced by the version used, nothing special actually. 

If you do not need the whole Alchemist machinery but just a sub-part of it, you can restrict the set of imported artifacts by using as dependencies the modules you are actually in need of.

### Detail: using Gradle to launch Alchemist (or any Java process...)

The following is a `runAlchemist` task, it is a simple gradle task responsible for launching the simulation.
Let's dissect it:
```kotlin
tasks.register<JavaExec>("runAlchemist") {
    classpath = project.sourceSets.getByName("main").runtimeClasspath
    main = "it.unibo.alchemist.Alchemist"
    args = listOf("-y", "src/main/yaml/$simulation.yml")
}
```
[Gradle](https://gradle.org) has a special task to run a Java class from the build script: `JavaExec`. We can create our custom task of type `JavaExec`, name it `runAlchemist` and configure it to launch our simulation. In order to make it work, we need to explicit two things:
- the Alchemist main class, which is `it.unibo.alchemist.Alchemist`
- the classpath, or java won't be able to find all the classes needed

This is what we do with the first three lines of code, and it is sufficient to successfully start Alchemist.
Now, to make it run our simulation we can rely on the [command line interface](#command-line-interface),
to run a simulation we can use the `-y` option followed by the path to the simulation file.
Let's suppose the `$simulation` variable contains the name of our simulation file,
which is located in the `src/main/yaml/` folder,
what we want to do is to run Alchemist with the following arguments:
```bash
-y src/main/yaml/$simulation.yml
```
The last line of code specify these arguments.

We recommend at first running alchemist with the `--help` option, which will display further information on the available commands.

