---

title: Running Alchemist stand-alone

---
Another way of running Alchemist is by using the redistributable jar file.
However, using the simulator via [Gradle](https://gradle.org) is recommended.
For further information, see how to {{ anchor('Running Alchemist via Gradle', 'Running Alchemist via Gradle') }}.

Such jar file can be downloaded from the [releases section on github](https://github.com/AlchemistSimulator/Alchemist/releases).

If you got the redistributable, runnable Alchemist jar file, launching the simulator is straightforward. Open a terminal and move to the folder where the jar is located, then issue:
```bash
java -jar alchemist-full-VERSION-all.jar
```
Remember to substitute `VERSION` with the Alchemist version you actually have downloaded.
You can still use alchemist in a modularized form using jars.
In this case, use `alchemist-VERSION-all.jar` and all the jars corresponding to the modules you need.
Pass them to the `java` command as classpath, e.g.:

```bash
java -cp alchemist-VERSION-all.jar:alchemist-incarnation-protelis-VERSION-all.jar:alchemist-swingui-VERSION-all.jar it.unibo.alchemist.Alchemist --help
```

Under Windows, the separator is `;` in place of `:`

This command will print information on the available command line options.
