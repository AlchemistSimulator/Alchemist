# Alchemist

![Alchemist logo](https://alchemistsimulator.github.io/latest/assets/media/logo.svg)

### Note for academics

The Alchemist simulator license obliges those who use this software for an academic publication to provide proper attribution.
This should be to the paper introducing Alchemist:

Pianini, D., Montagna, S., & Viroli, M. (2013). *Chemical-oriented simulation of computational systems with ALCHEMIST.*
Journal of Simulation, 7(3), 202–215. [https://doi.org/10.1057/jos.2012.27](https://doi.org/10.1057/jos.2012.27)

A BibTeX entry for LaTeX users is:

```bibtex
@article{Pianini2013,
  doi = {10.1057/jos.2012.27},
  url = {https://doi.org/10.1057/jos.2012.27},
  year = {2013},
  month = aug,
  publisher = {Informa {UK} Limited},
  volume = {7},
  number = {3},
  pages = {202--215},
  author = {D Pianini and S Montagna and M Viroli},
  title = {Chemical-oriented simulation of computational systems with {ALCHEMIST}},
  journal = {Journal of Simulation}
}
```

## What is Alchemist

Alchemist is a simulator for pervasive, aggregate, and nature-inspired computing.
At the moment, the simulator features:

* Executing [Protelis](http://protelis.org) programs
* Executing [Scafi](https://scafi.github.io/) programs
* Executing biological simulations with chemical-like reactions
* Executing [SAPERE](http://dx.doi.org/10.1016/j.pmcj.2014.12.002) -like programs, written in a tuple based language resembling [Linda](https://doi.org/10.1109%2Fmc.1986.1663305)
* Simulating bidimensional environments
* Simulating real-world maps, with support for navigation along roads, and for importing gpx format gps traces
* Simulating indoor environments by importing black and white images
* Simulating networks of smart cameras (similarly to [CamSim](https://doi.org/10.1109/SASOW.2013.11), but with much better scaling)
* Simulating pedestrian with a cognitive model
* Creating batches and run with different value of parameters
* Run in grid computing environments

Alchemist users should rely on the documentation available on [the official Alchemist website](http://alchemistsimulator.github.io/).
If you are already there, well, this text is shared by the README.md file and the site front page,
so you are on the right place, **check the menu** (should be on the left-hand side) to learn how to use the simulator.

If you need access to features of the simulator which are still in development,
please refer to the ["latest" version of the website](http://alchemistsimulator.github.io/latest).

Alchemist is available on Maven Central. You can import all the components by importing the `it.unibo.alchemist:alchemist` artifact.

### Gradle

You need to add the alchemist core dependency, plus the modules you need for your simulation.
Add this dependency to your build, substituting `ALCHEMIST_VERSION` with the version you want to use
(change the scope appropriately if you need Alchemist only for runtime or testing).

```kotlin
dependencies {
    // Alchemist core dependency
    implementation("it.unibo.alchemist:alchemist:ALCHEMIST_VERSION")
    // Example incarnation
    implementation("it.unibo.alchemist:alchemist-incarnation-protelis:ALCHEMIST_VERSION")
    // Example additional module
    implementation("it.unibo.alchemist:alchemist-cognitive-agents:ALCHEMIST_VERSION")
}
```

### Maven

Add this dependency to your build, substitute `ALCHEMIST_VERSION` with the version you want to use. If you do not need the whole Alchemist machinery but just a sub-part of it, you can restrict the set of imported artifacts by using as dependencies the modules you are actually in need of.

```xml
<dependencies>
    <dependency>
        <groupId>it.unibo.alchemist</groupId>
        <artifactId>alchemist</artifactId>
        <version>ALCHEMIST_VERSION</version>
    </dependency>
    <dependency>
        <groupId>it.unibo.alchemist</groupId>
        <artifactId>alchemist-incarnation-protelis</artifactId>
        <version>ALCHEMIST_VERSION</version>
    </dependency>
    <dependency>
        <groupId>it.unibo.alchemist</groupId>
        <artifactId>alchemist-cognitive-agents</artifactId>
        <version>ALCHEMIST_VERSION</version>
    </dependency>
</dependencies>
```

## Developers

### Status Badges

#### Stable branch

##### Info
master: [![Build Status](https://travis-ci.org/AlchemistSimulator/Alchemist.svg?branch=master)](https://travis-ci.org/AlchemistSimulator/Alchemist)

develop: [![Build Status](https://travis-ci.org/AlchemistSimulator/Alchemist.svg?branch=develop)](https://travis-ci.org/AlchemistSimulator/Alchemist)

[![Javadocs](https://www.javadoc.io/badge/it.unibo.alchemist/alchemist.svg)](https://www.javadoc.io/doc/it.unibo.alchemist/alchemist)
![GitHub](https://img.shields.io/github/license/AlchemistSimulator/Alchemist)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/TODO/badge)](https://bestpractices.coreinfrastructure.org/projects/TODO)
![GitHub language count](https://img.shields.io/github/languages/count/AlchemistSimulator/Alchemist)
![GitHub top language](https://img.shields.io/github/languages/top/AlchemistSimulator/Alchemist)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=ncloc)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/AlchemistSimulator/Alchemist)
![GitHub repo size](https://img.shields.io/github/repo-size/AlchemistSimulator/Alchemist)
![Maven Central](https://img.shields.io/maven-central/v/it.unibo.alchemist/alchemist)
![GitHub contributors](https://img.shields.io/github/contributors/AlchemistSimulator/Alchemist)

##### Quality

[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c7304e8bd4044aa5955c6d5c844f39a4)](https://www.codacy.com/app/Alchemist/Alchemist?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=AlchemistSimulator/Alchemist&amp;utm_campaign=Badge_Grade)
![Codecov](https://img.shields.io/codecov/c/github/AlchemistSimulator/Alchemist)
![Code Climate maintainability](https://img.shields.io/codeclimate/maintainability-percentage/AlchemistSimulator/Alchemist)
![Code Climate maintainability](https://img.shields.io/codeclimate/issues/AlchemistSimulator/Alchemist)
![Code Climate maintainability](https://img.shields.io/codeclimate/tech-debt/AlchemistSimulator/Alchemist)
[![CodeFactor](https://www.codefactor.io/repository/github/alchemistsimulator/alchemist/badge)](https://www.codefactor.io/repository/github/alchemistsimulator/alchemist)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=alert_status)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=bugs)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=code_smells)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=security_rating)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=sqale_index)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=AlchemistSimulator_Alchemist&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=AlchemistSimulator_Alchemist)

##### Progress
![GitHub issues](https://img.shields.io/github/issues/AlchemistSimulator/Alchemist)
![GitHub closed issues](https://img.shields.io/github/issues-closed/AlchemistSimulator/Alchemist)
![GitHub pull requests](https://img.shields.io/github/issues-pr/AlchemistSimulator/Alchemist)
![GitHub closed pull requests](https://img.shields.io/github/issues-pr-closed/AlchemistSimulator/Alchemist)
![GitHub commit activity](https://img.shields.io/github/commit-activity/y/AlchemistSimulator/Alchemist)
![GitHub commits since latest release (by date)](https://img.shields.io/github/commits-since/AlchemistSimulator/Alchemist/latest/develop)
![GitHub last commit](https://img.shields.io/github/last-commit/AlchemistSimulator/Alchemist/develop)

### Javadocs

Javadocs are available for both [the latest stable version][Javadoc] and [the latest development snapshot][Javadoc-unstable].
If you need to access the documentation for any older stable version, [javadoc.io](https://www.javadoc.io/doc/it.unibo.alchemist/alchemist/) is probably the right place to search in.


### Developing Alchemist (namely evolving the simulator, not using it for simulating stuff)

#### Forking the project
To contribute to this project we recommend to fork it and work on your own copy so that you can:

* push all your commits, saving your work on the cloud;
* exploit the included continuous integration jobs to check the project status;
* contribute back to the main project via pull requests directly from GitHub.

This project defines several CI jobs.
Some rely on an automatic merge system ([YAAGHA](https://github.com/marketplace/actions/yet-another-automerge-github-action)) which requires a repository secret with:

* name = AUTOMERGE_TOKEN
* value = a github token with `public_repo` scope (or `repo`, if your fork is private). If you already have a token with the required scope, then you can reuse it, otherwise you have to [create a new token](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token).

If you don't provide it, then the CI workflow is likely to fail even though the project status is correct.

#### Recommended IDE configuration

The project is easiest to import in IntelliJ Idea.

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

1. Clone this repository in a folder of your preference using `git clone --recurse-submodules <ALCHEMIST_REPO_URI>`.
1. Right click on `settings.gradle.kts`, select "Open With" and use IntelliJ Idea.
The procedure may be slightly different depending on your operating system and desktop environment.
If you have a terminal, and if you can launch idea from there, just:

    1. `cd <LOCATION_WHERE_YOU_CLONED_THE REPOSITORY>`
    1. `idea .` (we are assuming that you can launch IntelliJ Idea with the `idea` command, replace it with the correct one for your syustem
1. In 'Settings -> Build, Execution, Deployment -> Build Tools > Gradle', for the option 'Use Gradle from' select 'gradle-wrapper.properties file'. Enabling auto-import is also recommended.
1. **Important:** Alchemist requires java 11+, so make sure the 'Gradle JVM' option points to such a version (if you don't have a JDK 11+ installed make sure to get one).
1. Once imported, the project may still be unable to compile, due to missing sources in incarnation-biochemistry. This problem can be solved by opening the IntelliJ terminal (e.g. with <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>A</kbd>, typing "terminal" and pressing <kbd>Enter</kbd>), and issue:
  - On Unix: `./gradlew alchemist-incarnation-biochemistry:generateGrammarSource`
  - On Windows: `gradlew.bat alchemist-incarnation-biochemistry:generateGrammarSource`

### Developing the project
Contributions to this project are welcome. Just some rules:

* We use [git flow](https://github.com/nvie/gitflow), so if you write new features, please do so in a separate `feature-` branch.
* We recommend forking the project, developing your stuff, then contributing back via pull request directly from GitHub
* Commit often. Do not throw at me pull requests with a single giant commit adding or changing the whole world. Split it in multiple commits and request a merge to the mainline often.
* Stay in sync with the `develop` branch: pull often from `develop` (if the build passes), so that you don't diverge too much from the main development line.
* Do not introduce low quality code. All the new code must comply with the checker rules (that are quite strict) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.

#### Building the project
While developing, you can rely on IntelliJ to build the project, it will generally do a very good job.
If you want to generate the artifacts, you can rely on Gradle. Just point a terminal on the project's root and issue

```bash
./gradlew build
```

This will trigger the creation of the artifacts the executions of the tests, the generation of the documentation and of the project reports.

***NOTE THAT:*** Alchemist requires java 11+ to work, make sure to have a JDK version 11+ installed.

#### Build reports
Every Alchemist build triggers the creation of a set of reports, that provide hints regarding the current status of quality of the code base. Such reports are available for both [the latest stable][reports] and [the latest development][reports-unstable] versions.

[Alchemist]: http://alchemistsimulator.github.io/
[Javadoc]: http://alchemist-doc.surge.sh/
[Javadoc-unstable]: http://alchemist-unstable-doc.surge.sh/
[reports-unstable]: http://alchemist-unstable-reports.surge.sh/build/reports/buildDashboard/
[reports]: http://alchemist-reports.surge.sh/build/reports/buildDashboard/
[eclipse]: https://eclipse.org/downloads/
