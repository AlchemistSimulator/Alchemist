+++
pre = ""
title = "Build and run the QA"
weight = 30
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
`gradlew` or `gradlew.bat`. Windows users are likely to use the latter.

### Building the project

The project can get build via Gradle:

```bash
./gradlew assemble --parallel
```

When imported in IntelliJ Idea as Gradle project, the IDE will use Gradle under the hood to run the necessary steps to perform compilation and packaging.

### Testing

Testing can be executed by issuing
```bash
./gradlew test --parallel
```

### Quality Assurance

To perform a QA run
```bash
./gradlew check --parallel
```

### Generating the website

To generate the Alchemist website run
```bash
./gradlew hugoBuild --parallel
```
### Website preview

For a preview of the website issue:
```bash
./gradlew hugo --command=serve
```
The terminal output will show a link, most likely `https://localhost:1313/`, where the website is being served.
