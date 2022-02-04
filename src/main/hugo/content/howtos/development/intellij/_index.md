+++
pre = ""
title = "Import Alchemist in an IDE"
weight = 4
tags = ["import", "IDE", "IntelliJ"]
summary = "The recommended way to get and import the Alchemist project in an IDE"
+++
## Recommended IDE configuration

The project is easiest to import in IntelliJ Idea.
The project can be imported directly as a Gradle project.
If you intend to develop new parts in Scala, we suggest to install the Scala plugin for IntelliJ Idea.

## Forking the project
To contribute to the Alchemist project you must
[fork it](https://github.com/AlchemistSimulator/Alchemist/fork) and work on your own
copy. In this way you can:

* push all your commits, saving your work on the cloud;
* exploit the included continuous integration jobs to check the project status;
* contribute back to the main project via pull requests directly from GitHub.

## Importing the project

1. Clone either the [Alchemist repository](https://github.com/AlchemistSimulator/Alchemist) 
   or your personal fork in a folder of your preference using `git clone --recurse-submodules <ALCHEMIST_REPO_URI>`.
1. Right click on `settings.gradle.kts`, select "Open With" and use IntelliJ Idea.

The procedure may be slightly different depending on your operating system and desktop environment.

If you have a terminal, and if you can launch idea from there, just:

1. `cd <LOCATION_WHERE_YOU_CLONED_THE REPOSITORY>`
1. `idea .` (we are assuming that you can launch IntelliJ Idea with the `idea` command, replace it with the correct one for your system)


