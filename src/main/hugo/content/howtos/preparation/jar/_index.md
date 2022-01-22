+++
pre = ""
title = "Alchemist stand-alone"
weight = 5
tags = ["gradle", "run", "launch", "example", "jar", "shadowjar", "fatjar"]
summary = "The recommended way to run the simulator and fetch all the required modules."
+++

Although we recommend to [run the simulator via Gradle](../gradle/),
Alchemist can be executed through the redistributable jar file.

Such jar file can be downloaded from the [releases section on github](https://github.com/AlchemistSimulator/Alchemist/releases).

Obtain [the runnable jar of alchemist-full from GitHub](https://github.com/AlchemistSimulator/Alchemist/releases/latest/),
the open a terminal and move to the folder where the jar is located, then issue:
```bash
java -jar alchemist-full-VERSION-all.jar --help
```
Remember to substitute `VERSION` with the Alchemist version you actually have downloaded.
You can still use alchemist in a modularized form using jars.
In this case, use `alchemist-VERSION-all.jar` and all the jars corresponding to the modules you need.
Pass them to the `java` command as classpath, e.g.:

```bash
java -cp alchemist-VERSION-all.jar:alchemist-incarnation-protelis-VERSION-all.jar:alchemist-swingui-VERSION-all.jar it.unibo.alchemist.Alchemist --help
```

Under Windows, the separator is `;` in place of `:`

This command will print information on the available [command line options](/reference/cli/).
