+++
title = "Create reusable variables"
weight = 1
tags = ["variable", "metric", "dry", "language", "groovy", "kotlin", "scala", "jsr223"]
summary = "Define reusable pieces of information and compute upon them, prepare for the execution of simulation batches."
+++

## Free variables

Alchemist provides first-class support for executing multiple simulations with  varying conditions.
Variables can be listed in the variables section of the simulation descriptor.

Every variable has a default value and a way to generate other values.
When a batch execution is requested, the cartesian product of all
possible values for the selected variables is produced, the default values are used
for non-selected variables.
Then, for each entry, a simulation is prepared and
then executed (execution can be and usually is performed in parallel).

## Dependent variables

Moreover, to favor reusability and
apply the DRY principle, the simulator allows defining variables whose values
possibly depend on values of other variables.
Their values can be expressed, by default, in Groovy, but any JSR-223-compatible language can be used, in principle.

{{% notice info %}}
The [JSR-223 specification](https://jcp.org/en/jsr/detail?id=223) defines mechanisms
allowing scripting language programs to access information developed in the Java Platform.

Many languages (including [Groovy](https://groovy-lang.org/integrating.html#jsr223),
[Python (Jython)](https://wiki.python.org/jython/UserGuide#using-jsr-223),
[Kotlin](https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/jsr223/jsr223.md),
and Scala provide bindings for JSR-223).
If a compatible JSR-223 implementation of the language is available in the classpath,
Alchemist will load and use it transparently.

By default, `groovy`, `kotlin` (or `kts`), and `scala` are available as scripting languages for dependent variables.
{{% /notice %}}
