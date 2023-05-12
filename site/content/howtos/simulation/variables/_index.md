+++
title = "Create reusable variables"
weight = 1
tags = ["variable", "metric", "dry", "language", "groovy", "kotlin", "scala", "jsr223"]
summary = "Define reusable pieces of information and compute upon them, prepare for the execution of simulation batches."
+++

## Declaring variables

The [`variables`](/reference/yaml/#variables) section lists variable simulation values.
A variable defines some kind of value that can be referenced in the simulation file.

There are two kinds of variables: free and dependent variables.
Free variables are meant to provide support for running batches of simulations with varying parameters;
dependent variables are either single valued or can be computed from the values of other variables
(free or dependent), and they are designed to simplify the simulation code.

## Free variables

Free variables define a set of values and a default.
Their main scope is enabling Alchemist to run a set of simulations with different parameters (values of variables)
without the need to duplicate the simulation code.
When used in this mode (called "batch mode"),
Alchemist by default produces the cartesian product of all the variables values' selected for the batch,
and runs a simulation for each combination.
If the simulation is not executed as batch, then the default value is used.

### Linear variables

A variable generating values in a range, starting from a minimum value, and increasing by some step.
Represented by {{% api package="boundary.variables" class="LinearVariable" %}}.

#### Examples

{{< code path="alchemist-loading/src/test/resources/guidedTour/linearVariableMinimal.yml" >}}
{{< code path="alchemist-loading/src/test/resources/isac/14-yaml-vars.yml" >}}
{{< code path="src/test/resources/website-snippets/variables-export.yml" >}}
{{< code path="alchemist-incarnation-protelis/src/test/resources/18-export.yml" >}}
{{< code path="alchemist-incarnation-protelis/src/test/resources/test00.yml" >}}
{{< code path="alchemist-incarnation-protelis/src/test/resources/tomacs.yml" >}}
{{< code path="alchemist-loading/src/test/resources/guidedTour/linearVariableRequiringConstant.yml" >}}
{{< code path="alchemist-loading/src/test/resources/isac/15-move.yml" >}}
{{< code path="alchemist-loading/src/test/resources/synthetic/convoluted_variables.yml" >}}

### Geometric variables

A variable generating geometrically-distributed samples across a range.
Ideal for exploring non-linear phenomena, or for exploring very large ranges of values whose effect is unknown.
Implemented as {{% api package="boundary.variables" class="GeometricVariable" %}}.

#### Examples

{{< code path="alchemist-loading/src/test/resources/synthetic/singleValuedGeometricVar.yml" >}}
{{< code path="src/test/resources/website-snippets/variables-export.yml" >}}
{{< code path="alchemist-loading/src/test/resources/isac/14-yaml-vars.yml" >}}
{{< code path="alchemist-loading/src/test/resources/isac/15-move.yml" >}}

### Arbitrary-valued variables

Generates an {{% api package="boundary.variables" class="ArbitraryVariable" %}} spanning on an arbitrary set of values.

#### Examples

{{< code path="src/test/resources/simplesimulation.yml" >}}
{{< code path="alchemist-loading/src/test/resources/synthetic/convoluted_variables.yml" >}}

## Dependent variables

Some variables are combination of other variables.
Let's suppose that we want to deploy on a circle,
but for some reason
(e.g. because it is required by the constructor of some {{% api class="Action" %}})
we need to compute and have available radius and perimeter.
We don't need to control both of them: the perimeter can be computed from the radius
(or vice versa).

To favor reusability and
apply the DRY principle, the simulator allows defining variables whose values
possibly depend on values of other variables through {{% api package="boundary.variables" class="JSR223Variable" %}}.
Their values can be expressed, by default, in Groovy, but any JSR-223-compatible language can be used, in principle.
If a compatible JSR-223 implementation of the language is available in the classpath,
Alchemist will load and use it transparently.
By default, `groovy`, `kotlin` (or `kts`), and `scala` are available as scripting languages for dependent variables.

{{% notice info %}}
The [JSR-223 specification](https://jcp.org/en/jsr/detail?id=223) defines mechanisms
allowing scripting language programs to access information developed in the Java Platform.

Many languages (including [Groovy](https://groovy-lang.org/integrating.html#jsr223),
[Python (Jython)](https://wiki.python.org/jython/UserGuide#using-jsr-223),
[Kotlin](https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/jsr223/jsr223.md),
and Scala provide bindings for JSR-223).
{{% /notice %}}

Variables can be defined in any order.
Alchemist figures out the dependencies automatically, as far as there are no cyclic dependencies (e.g. variable `a` requires `b`, and `b` requires `a`).
Please note that the simulator variable dependency resolution system is not designed to solve mathematical systems,
so even though the problem has a well-formed mathematical solution, the actual variable resolution may fail;
e.g. if `a` is defined as `2 * b + 1`, and `b` is defined as `4 - a`, the system **won't** bind `a` to `3` and `b` to `1`,
but will simply fail complaining about circular dependencies.

{{% notice tip "Multiline programs" %}}
Sometimes data manipulation can get tricky and trivial scripting may no longer be enough.
In such cases, and especially with modern languages that allow for a reduced usage of cerimonial semicolons
(such as Kotlin and Scala), it can be useful to write multiline programs.
This can be achieved in YAML by using the pipe `|` operator, as exemplified in the following snippet:
```yaml
multiline-string: |
  note that the string
  needs to be indented.
  Newlines will be preserved!
```
{{% /notice %}}

#### Examples

{{< code path="alchemist-loading/src/test/resources/synthetic/convoluted_variables.yml" >}}
{{< code path="alchemist-loading/src/test/resources/synthetic/multiplemolecule.yml" >}}
{{< code path="alchemist-loading/src/test/resources/synthetic/scalavar.yml" >}}
{{< code path="alchemist-loading/src/test/resources/testktvar.yml" >}}
{{< code path="alchemist-loading/src/test/resources/synthetic/testlist.yml" >}}
{{< code path="alchemist-loading/src/test/resources/synthetic/varcontentclash.yml" >}}
{{< code path="src/test/resources/website-snippets/variables-export.yml" >}}
{{< code path="src/test/resources/website-snippets/pedestrian-groups.yml" >}}
{{< code path="src/test/resources/website-snippets/evacuation-scenarios.yml" >}}
{{< code path="alchemist-maps/src/test/resources/simulations/connect-sight.yml" >}}
{{< code path="alchemist-loading/src/test/resources/guidedTour/linearVariableRequiringConstant.yml" >}}
{{< code path="alchemist-loading/src/test/resources/isac/14-yaml-vars.yml" >}}
{{< code path="alchemist-loading/src/test/resources/isac/15-move.yml" >}}
{{< code path="src/test/resources/website-snippets/variables-export.yml" >}}
