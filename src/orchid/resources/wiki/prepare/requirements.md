---

title: Requirements

---

## Requirements

Alchemist requires java 11 or later,
if you don't have a Java Development Kit version 11+ installed we may recommend you to install the version of AdoptOpenJDK,
which can be found [here](https://adoptopenjdk.net/index.html?variant=openjdk11&jvmVariant=hotspot).

If you prefer, you can install it via [Jabba](https://github.com/shyiko/jabba) - a cross-platform Java Version Manager.

In the following, we will assume you have Java correctly installed and configured on your system:
* the `java -version` command should output a version 11 or above
* the `javac -version` command should output version 11 or above

It's also a good idea to have [`git`](https://git-scm.com/) installed on your PC and available:
we provide templates and examples via Git repositories,
it makes it easier to import them.

### Next step

There are two ways of running Alchemist:

* [Via Gradle](../../use/run-gradle): the recommended way, as it provides an easy way to import the simulator within an
IDE and develop your simulation from there. 
* [Stand-alone](../../use/run-stand-alone): an alternative that can be useful for quick demos of if you just need to run
an existing simulation file.
