+++
pre = ""
title = "Build and run the QA"
weight = 5
tags = ["build", "report", "IntelliJ", "Gradle", "Javadoc"]
summary = "How to locally build and test the simulator"
+++

## Building the project
While developing, you can rely on IntelliJ to build the project, it will generally do a very good job.
If you want to generate the artifacts, you can rely on Gradle. Just point a terminal on the project's root and issue

```bash
./gradlew assemble check --parallel
```

## Build reports
Every Alchemist build triggers the creation of a set of reports, that provide hints regarding the current status of 
quality of the code base. Such reports are available for both [the latest stable][reports] and 
[the latest development][reports-unstable] versions.

[reports-unstable]: http://alchemist-unstable-reports.surge.sh/build/reports/buildDashboard/
[reports]: http://alchemist-reports.surge.sh/build/reports/buildDashboard/