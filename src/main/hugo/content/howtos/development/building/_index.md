+++
pre = ""
title = "Build and run the QA"
weight = 5
tags = ["build", "report", "IntelliJ", "Gradle", "Javadoc"]
summary = "How to locally build and test the simulator"
+++
## Building with Gradle
The recommended way to execute any Gradle build is with the help of the Gradle
Wrapper (in short just “Wrapper”). The Wrapper is a script that invokes a declared
version of Gradle, downloading it beforehand if necessary. As a result, developers
can get up and running with a Gradle project quickly without having to follow manual
installation. For more information please refer to the [Gradle's documentation website](https://docs.gradle.org/current/userguide/gradle_wrapper.html).
### gradlew vs. gradlew.bat
Depending on which scripting environment you are using the wrapper can be invoked with
`gradlew` or `gradlew.bat`. Windows users are most likely to use the latter.

### Building the project
While developing, you can rely on IntelliJ to build the project, it will generally do a very good job.
If you want to generate the artifacts, you can rely on Gradle. Just point a terminal on the project's root and issue

```bash
./gradlew assemble --parallel
```

### Testing
Testing can be executed by issuing
```bash
./gradlew test
```

### Quality Assurance
To perform a QA run
```bash
./gradlew check
```

### Generating the website
To generate the Alchemist website run
```bash
./gradlew hugoBuild
```
### Website preview
For a preview of the website issue
```bash
./gradlew hugo --comand=serve
```

## Build reports
Every Alchemist build triggers the creation of a set of reports, that provide hints regarding the current status of 
quality of the code base. Such reports are available for both [the latest stable][reports] and 
[the latest development][reports-unstable] versions.

[reports-unstable]: http://alchemist-unstable-reports.surge.sh/build/reports/buildDashboard/
[reports]: http://alchemist-reports.surge.sh/build/reports/buildDashboard/