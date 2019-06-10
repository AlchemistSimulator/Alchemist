# Alchemist

![Alchemist logo](https://alchemistsimulator.github.io/latest/assets/media/logo.svg)

Alchemist is a simulator for pervasive, aggregate, and nature-inspired computing.
At the moment, the simulator features:

* Executing [Protelis](http://protelis.org) programs
* Executing [Scafi](https://scafi.github.io/) programs
* Executing biological simulations with chemical-like reactions
* Executing [SAPERE](http://dx.doi.org/10.1016/j.pmcj.2014.12.002) -like programs, written in a tuple based language resembling [Linda](https://doi.org/10.1109%2Fmc.1986.1663305)
* Simulating bidimensional environments
* Simulating real-world maps, with support for navigation along roads, and for importing gpx format gps traces
* Simulating indoor environments by importing black and white images
* Creating batches and run with different value of parameters
* Run in grid computing environments

## Users

Alchemist users should rely on the documentation available on [the official Alchemist website](http://alchemistsimulator.github.io/).
Alchemist is available on Maven Central. You can import all the components by importing the `it.unibo.alchemist:alchemist` artifact.

### Gradle

Add this dependency to your build, substituting `ALCHEMIST_VERSION` with the version you want to use (change the scope appropriately if you need Alchemist only for runtime or testing).

```kotlin
dependencies {
    implementation("it.unibo.alchemist:alchemist:ALCHEMIST_VERSION")
}
```

### Maven

Add this dependency to your build, substitute `ALCHEMIST_VERSION` with the version you want to use. If you do not need the whole Alchemist machinery but just a sub-part of it, you can restrict the set of imported artifacts by using as dependencies the modules you are actually in need of.

```xml
<dependency>
    <groupId>it.unibo.alchemist</groupId>
    <artifactId>alchemist</artifactId>
    <version>ALCHEMIST_VERSION</version>
</dependency>
```

## Developers

### Status Badges

#### Stable branch
[![Build Status](https://travis-ci.org/AlchemistSimulator/Alchemist.svg?branch=master)](https://travis-ci.org/AlchemistSimulator/Alchemist)
[![Javadocs](https://www.javadoc.io/badge/it.unibo.alchemist/alchemist.svg)](https://www.javadoc.io/doc/it.unibo.alchemist/alchemist)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c7304e8bd4044aa5955c6d5c844f39a4)](https://www.codacy.com/app/Alchemist/Alchemist?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=AlchemistSimulator/Alchemist&amp;utm_campaign=Badge_Grade)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

#### Development branch
[![Build Status](https://travis-ci.org/AlchemistSimulator/Alchemist.svg?branch=develop)](https://travis-ci.org/AlchemistSimulator/Alchemist)

### Javadocs 

Javadocs are available for both [the latest stable version][Javadoc] and [the latest development snapshot][Javadoc-unstable].
If you need to access the documentation for any older stable version, [javadoc.io](https://www.javadoc.io/doc/it.unibo.alchemist/alchemist/) is probably the right place to search in.


### Importing the project
The project is easiest to import in IntelliJ Idea.

#### Recommended configuration
Install the following plugins (use <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>A</kbd>, then search for "Plugins"):
* From the main list:
    * Scala
    * Kotlin
* From "Browse Repositories":
    * [ANTLR v4 grammar plugin](https://plugins.jetbrains.com/plugin/7358-antlr-v4-grammar-plugin)
    * [Checkstyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
    * [FindBugs-IDEA](https://plugins.jetbrains.com/plugin/3847-findbugs-idea)
    * [PMDPlugin](https://plugins.jetbrains.com/plugin/1137-pmdplugin)

#### Importing the project

0. Clone this repository in a folder of your preference using `git clone` appropriately
0. Open IntellJ. If a project opens automatically, select "Close project". You should be on the welcome screen of IntelliJ idea, with an aspect similar to this image: ![IntelliJ Welcome Screen](https://www.jetbrains.com/help/img/idea/2018.2/ideaWelcomeScreen.png)
0. Select "Import Project"
0. Navigate your file system and find the folder where you cloned the repository. **Do not select it**. Open the folder, and you should find a lowercase `alchemist` folder. That is the correct project folder, not the outermost `Alchemist` folder (created by `git` in case you cloned without specifying a different folder name). Once the correct folder has been selected, click <kbd>Ok</kbd>
0. Select "Import Project from external model"
0. Make sure "Gradle" is selected as external model tool
0. click <kbd>Next</kbd>
0. Check "Use auto-import"
0. Check "Create directories for empty content roots automatically"
0. The correct group module option is "using explicit module groups", make sure it is selected
0. Check "Create separate module per source set"
0. *Important:* ensure that "Use default gradle wrapper (recommended) is selected
0. Click <kbd>Finish</kbd>
0. If prompted to override any .idea file, try to answer <kbd>No</kbd>. It's possible that IntelliJ refuses to proceed and comes back to the previous window, in which case click <kbd>Finish</kbd> again, then select <kbd>Yes</kbd>.
0. Wait for the IDE to import the project from Gradle. The process may take several minutes, due to the amount of dependencies. Should the synchronization fail, make sure that the IDE's Gradle is configured correctly. In 'Settings -> Build, Execution, Deployment -> Build Tools > Gradle', select 'Use default gradle wrapper (recommended)'.
0. Once imported, the project may still be unable to compile, due to missing sources in incarnation-biochemistry. This problem can be solved by opening the IntelliJ terminal (e.g. with <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>A</kbd>, typing "terminal" and pressing <kbd>Enter</kbd>), and issue:
  - *On Unix: * `./gradlew alchemist-incarnation-biochemistry:generateGrammarSource`
  - *On Windows: * `gradlew.bat alchemist-incarnation-biochemistry:generateGrammarSource`

### Developing the project
Contributions to this project are welcome. Just some rules:

* We use [git flow](https://github.com/nvie/gitflow), so if you write new features, please do so in a separate `feature-` branch.
* We recommend forking the project, developing your stuff, then contributing back via pull request directly from GitHub
* Commit often. Do not throw at me pull requests with a single giant commit adding or changing the whole world. Split it in multiple commits and request a merge to the mainline often.
* Stay in sync with the `develop` branch: pull often from `develop` (if the build passes), so that you don't diverge too much from the main development line.
* Do not introduce low quality code. All the new code must comply with the checker rules (that are quite strict) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.


#### Building the project
While developing, you can rely on Eclipse to build the project, it will generally do a very good job.
If you want to generate the artifacts, you can rely on Gradle. Just point a terminal on the project's root and issue

```bash
./gradlew
```

This will trigger the creation of the artifacts the executions of the tests, the generation of the documentation and of the project reports.

#### Build reports
Every Alchemist build triggers the creation of a set of reports, that provide hints regarding the current status of quality of the code base. Such reports are available for both [the latest stable][reports] and [the latest development][reports-unstable] versions.

[Alchemist]: http://alchemistsimulator.github.io/
[Javadoc]: http://alchemist-doc.surge.sh/
[Javadoc-unstable]: http://alchemist-unstable-doc.surge.sh/
[reports-unstable]: http://alchemist-unstable-reports.surge.sh/build/reports/buildDashboard/
[reports]: http://alchemist-reports.surge.sh/build/reports/buildDashboard/
[eclipse]: https://eclipse.org/downloads/
