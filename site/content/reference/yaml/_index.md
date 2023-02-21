+++
pre = ""
title = "YAML simulation specification"
weight = 1
summary = "Specification of the YAML-based language simulations are configured with."
tags = ["yaml", "simulations", "specification", "environment", "extension", "programming"]
+++

{{% notice warning %}}
This reference guide assumes that the reader knows the basics of [YAML](https://yaml.org/).
A good resource to learn it quickly is  [Learn X in Y minutes where X = YAML](https://learnxinyminutes.com/docs/yaml/)
{{% /notice %}}

{{% notice info "Reading this document" %}}
The key words **MUST**, **MUST NOT**, **REQUIRED**, **SHALL**, **SHALL NOT**, **SHOULD**, **SHOULD NOT**,
**RECOMMENDED**,  **MAY**, and **OPTIONAL** in this document are to be interpreted as described in
[RFC 2119](https://datatracker.ietf.org/doc/html/rfc2119).
{{% /notice %}}

## Simulation document structure

The document **MUST** be YAML map.
The map **MUST** contain all the mandatory Alchemist keys,
**MAY** contain any subset of the optional Alchemist keys,
**MAY** contain any key whose name begins with underscore (`_`),
and **MUST NOT** contain any other key.

The sets of valid cobinations of mandatory and optional keys for each section of the document is specified
in form of Kotlin code as follows:

{{< code path="alchemist-loading/src/main/kotlin/it/unibo/alchemist/loader/m2m/syntax/Syntax.kt" >}}

### Types of entries

| Type        | Description                                                                                                                                                                                        |
|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Any         | Any YAML type                                                                                                                                                                                      |
| Int         | YAML integer number, or other type that can be parsed into an integer                                                                                                                              |
| List        | Any YAML List                                                                                                                                                                                      |
| Map         | Any YAML Map                                                                                                                                                                                       |
| MultiSpec   | A list of Spec. A Map matches a MultiSpec if it matches one and only one of its Spec.                                                                                                              |
| Number      | YAML number                                                                                                                                                                                        |
| Spec        | Pair of lists of strings. The first list contains mandatory keys, the second optional keys. A map matches a Spec if it contains all its mandatory keys, any of the optional keys, and no other key |
| SpecMap     | A YAML Map matching a MultiSpec                                                                                                                                                                    |
| String      | YAML String                                                                                                                                                                                        |
| Traversable | One of: A SpecMap, a List of Traversable, a Map of Traversable                                                                                                                                     |

---

### Arbitrary class loading system

**Type**: SpecMap

Alchemist is able to load arbitrary types conforming to the expected `interface`
(or Scala `trait`).
The expected type depends on where the class is requested.
This section describes how the system works independently of the specific target type.

**(Multi)Spec**

| Mandatory keys | Optional keys |
|----------------|---------------|
| `type`         | `parameters`  |

#### `type`

**Type**: String

The name of an instanceable class compatible with the expected interface.
It can be either the qualified name or a simple name,
in the latter case the class **SHOULD** be located in the same package where the
default alchemist implementations of the same interface live.

If a name includes a `.`, it is interpreted as a fully qualified name.
Otherwise, it is interpreted as a simple name.
Provided types **SHOULD NOT** be located in the default package.

For instance, if the expected type is an {{% api class="Action" %}} and the concrete type `FooAction`,
`FooAction` **SHOULD** be located into package {{% api package="model.implementations.actions" %}}.

#### `parameters`

**Type**: List or Map

The list of parameters the constructor of [type](#type) should be passed.
Alchemist automatically provides *contextual* information to the constructors:
for instance, if an {{% api class="Action" %}} is being built,
the loading system is aware of the current
`RandomGenerator`,
{{% api class="Incarnation" %}},
{{% api class="Environment" %}},
{{% api package="loader.deployments" class="Deployment" %}},
{{% api class="Node" %}}, {{% api class="TimeDistribution" %}}, and
{{% api class="Reaction" %}},
as the action requires all of them.
Consequently, all parameters of these types **SHOULD NOT** be manually specified
(on the other hand, the syntax is built to make it very difficult to do by mistake).
The constructor **MAY** fail if they are provided.

If a Map is provided instead of a List,
then the keys are interpred as the parameter names,
and their associated values as the corresponding parameter values.
Since Java 11 does not support named arguments,
this special invocation type is built around the Kotlin reflection,
thus, the concrete class whose constructor is being invoked **MUST** be written in Kotlin.

When using named arguments,
if at least one optional parameter is specified,
then all the previous optional parameters **MUST** be specified as well.
This limitation is due to the fact that Alchemist supports loading of JVM classes regardless of their origin language,
and, thus, the simulator must leverage constructor overloading to emulate optional parameters.
In the case of Kotlin classes, because of the way
[`@JvmOverloads`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/)
works, only a (reasonable) subset of all possible overloads gets generated, and they differ by parameter count.

Instantiation is delegated to the [Java Implicit Reflective Factory](https://github.com/DanySK/jirf/).

#### Examples

* Construction of a {{% api package="loader.deployments" class="Point" %}}
    {{<code path="src/test/resources/website-snippets/deployment-in-point.yml" >}}
* Construction of variables with named parameters
    {{<code path="src/test/resources/website-snippets/named-parameters.yml" >}}

#### Counter-examples

* The following simulation **fails on loading**, as
  {{% api package="model.implementations.layers" class="BidimensionalGaussianLayer" %}}
  has the *first* and *last* parameters marked as optional:
  in order to provide the latter, the designer must also provide the former.
    {{< code path="alchemist-loading/src/test/resources/guidedTour/optional-named-arguments.yml" >}}

---

### Document root

**Type**: SpecMap

The document contents at the root of the file. Builds an
{{% api package="loader" class="InitializedEnvironment" %}}.

**(Multi)Spec**

| Mandatory keys | Optional keys                                                                                                               |
|----------------|-----------------------------------------------------------------------------------------------------------------------------|
| `incarnation`  | `deployments`, `environment`, `export`, `layers`, `network-model`, `remote-dependencies`, `seeds`, `terminate`, `variables` |

#### Examples

* Minimal Biochemistry specification
  {{<code path="src/test/resources/website-snippets/minimal-biochemistry.yml" >}}
* Minimal Protelis specification
  {{<code path="src/test/resources/website-snippets/minimal-protelis.yml" >}}
* Minimal SAPERE specification
  {{<code path="src/test/resources/website-snippets/minimal-sapere.yml" >}}

---

### `incarnation`

**Type**: String

Valid incarnation types in the full distribution:
* `biochemistry`
* `protelis`
* `sapere`
* `scafi`

#### Examples

* Minimal Biochemistry specification
  {{<code path="src/test/resources/website-snippets/minimal-biochemistry.yml" >}}
* Minimal Protelis specification
  {{<code path="src/test/resources/website-snippets/minimal-protelis.yml" >}}
* Minimal SAPERE specification
  {{<code path="src/test/resources/website-snippets/minimal-sapere.yml" >}}

---

## `action`

Builds an {{% api class="Action" %}}
using the [arbitrary class loading system](#arbitrary-class-loading-system).

---

## `condition`

Builds a {{% api class="Condition" %}}
using the [arbitrary class loading system](#arbitrary-class-loading-system).

---

### `deployments`

**Type**: Traversable

Traversable of [`deployment`](#deployment)

### `deployment`

**Type**: SpecMap

Definition of the positions of a set of nodes.
Builds a 
{{% api package="loader.deployments" class="Deployment" %}}
using the same syntax of [arbitrary class loading system](#arbitrary-class-loading-system),
with additional keys.

**(Multi)Spec**

| Mandatory keys | Optional keys                                 |
|----------------|-----------------------------------------------|
| `type`         | `parameters`, `contents`, `nodes`, `programs` |


#### Examples

* Deployment of a single node in a point
  {{<code path="src/test/resources/website-snippets/deployment-in-point.yml" >}}
* Deployment of three nodes
  {{<code path="src/test/resources/website-snippets/deployment-in-three-points.yml" >}}
* Deployment of three nodes, but nesting the traversable
  {{<code path="src/test/resources/website-snippets/deployment-in-three-points-nested.yml" >}}
* Deployment of three nodes through {{% api package="loader.deployments" class="SpecificPositions" %}}.
  {{<code path="src/test/resources/website-snippets/deployment-specific-positions.yml" >}}
* {{% api package="loader.deployments" class="Grid" %}} centered in `(0, 0)`, with nodes distanced of `0.25` both horizontally and vertically.
  {{<code path="src/test/resources/website-snippets/deployment-grid.yml" >}}
* Irregular {{% api package="loader.deployments" class="Grid" %}} centered in `(0, 0)`, with nodes distanced of `0.25` both horizontally and vertically, randomly perturbed of (±`0.1` distance units).
  {{<code path="src/test/resources/website-snippets/deployment-grid-perturbed.yml" >}}
* Nodes located randomly inside a {{% api package="loader.deployments" class="Circle" %}}
  {{<code path="src/test/resources/website-snippets/deployment-circle.yml" >}}
* Nodes located randomly inside a {{% api package="loader.deployments" class="Rectangle" %}}
  {{<code path="src/test/resources/website-snippets/deployment-rectangle.yml" >}}
* Nodes located randomly inside a {{% api package="loader.deployments" class="Polygon" %}} delimiting the Venice Lagoon
  {{<code path="src/test/resources/website-snippets/maps-simple.yml" >}}

---

### `deployment.type`

Same as [type](#type)

### `deployment.parameters`

Same as [parameters](#parameters)

### `deployment.contents`

**Type**: Traversable of [`content`](#content)

### `deployment.nodes`

**Type**: SpecMap

Forces the type of {{% api class="Node" %}}, building concrete types through
the [arbitrary class loading system](#arbitrary-class-loading-system).
If left unspecified, nodes get created through
{{% api class="Incarnation" method="createNode" %}}.

#### Examples

* Creation of heterogeneous pedestrians
  {{<code path="alchemist-cognitive-agents/src/test/resources/heterogeneous-pedestrians.yml" >}}

### `deployment.properties`

**Type**: Traversable of [`property`](#property)

### `deployment.programs`

**Type**: Traversable of [`program`](#program)

---

### `content`

**Type**: SpecMap

Definition of the contents ({{% api class="Molecule" %}}s and {{% api class="Concentration" %}}s) of a group of nodes.

**(Multi)Spec**

| Mandatory keys              | Optional keys |
|-----------------------------|---------------|
| `molecule`, `concentration` | `in`          |

#### Examples
* Three molecules injected into all nodes deployed in the scenario
    {{<code path="alchemist-incarnation-protelis/src/test/resources/gradient.yml" >}}
* Injection of a molecule only in those nodes located inside a {{% api package="loader.filters" class="Rectangle" %}}
    {{<code path="src/test/resources/website-snippets/grid-dodgeball.yml" >}}

### `content.molecule`

**Type**: String or SpecMap

The name of the molecule to be injected.
If a String is provided, then it is created via {{% api class="Incarnation" method="createMolecule" %}}.
Otherwise, the [arbitrary class loading system](#arbitrary-class-loading-system) **SHOULD** be used.

### `content.concentration`

**Type**: String

The concentration of the molecule to be injected.
If a String is provided, then it is created via {{% api class="Incarnation" method="createConcentration" %}}.
Otherwise, the [arbitrary class loading system](#arbitrary-class-loading-system) **SHOULD** be used.

### `content.in`

**Type**: Traversable of [shapeFilter](#shapefilter)

### `property`

**Type**: SpecMap

**(Multi)Spec**

| Mandatory keys | Optional keys      |
|----------------|--------------------|
| `type`         | `parameters`, `in` |

### `property.type`

Same as [type](#type)

### `property.parameters`

Same as [parameters](#parameters)

### `property.in`

**Type**: Traversable of [shapeFilter](#shapefilter)

---

### `environment`

**Type**: SpecMap

Builds an {{% api class="Environment" %}}
using the same syntax of [arbitrary class loading system](#arbitrary-class-loading-system).

If left unspecified, defaults to a bidimensional Euclidean manifold:
{{% api package="model.implementations.environments" class="Continuous2DEnvironment" %}}.

**Type**: SpecMap

#### Examples

* Default environment, omitted specification
    {{<code path="src/test/resources/website-snippets/minimal-protelis.yml" >}}
* Explicitly builds a {{% api package="model.implementations.environments" class="Continuous2DEnvironment" %}} solely with the contextual parameters
  {{<code path="src/test/resources/website-snippets/envtype-protelis.yml" >}}
* Explicitly builds a {{% api package="model.implementations.environments" class="Continuous2DEnvironment" %}} using the qualified type name using only the contextual parameters
  {{<code path="src/test/resources/website-snippets/envtype-fullyqualified-protelis.yml" >}}
* Explicitly builds a {{% api package="model.implementations.environments" class="Continuous2DEnvironment" %}} explicitly specifying that no parameters but the contextual ones should be used
  {{<code path="src/test/resources/website-snippets/envtype-explicitparameters-protelis.yml" >}}

---

### `export`

**Type**: Traversable of [`exporter`](#exporter)

---

### `exporter`

**Type**: SpecMap

Definition of the contents ({{% api class="Molecule" %}}s and {{% api class="Concentration" %}}s) of a group of nodes.

**(Multi)Spec**

| Mandatory keys | Optional keys |
|----------------|---------------|
| `type`, `data` | `parameters`  |

### `exporter.type`

Same as [type](#type)

### `exporter.data`

**Type**: Traversable of [`extractor`](#extractor)

### `exporter.parameters`

Same as [parameters](#parameters)

---

### `extractor`

**Type**: String or SpecMap

The only supported String is `"time"`.
Otherwise, a SpecMap **MUST** be provided.

Creates instances of {{% api package="loader.export" class="Extractor" %}}.

**(Multi)Spec**

| Mandatory keys | Optional keys                             |
|----------------|-------------------------------------------|
| `type`         | `parameters`                              |
| `molecule`     | `property`, `aggregators`, `value-filter` |

### `extractor.type`

Same as [type](#type)

### `extractor.parameters`

Same as [parameters](#parameters)

### `extractor.molecule`

**Type**: String

Name of a {{% api class="Molecule" %}} to be read from nodes and exported.
The String is passed down to {{% api class="Incarnation" method="createMolecule" %}}.
The created molecule is read from every node.

### `extractor.property`

**Type**: String

Name of a property to be extracted from the selected {{% api class="Molecule" %}}.
The Molecule and the String are passed down to {{% api class="Incarnation" method="getProperty" %}}.
The obtained value is added to the exports.

### `extractor.aggregators`

**Type**: String or List of Strings

Name of any valid
[`UnivariateStatistic`](https://javadoc.io/static/org.apache.commons/commons-math3/3.6.1/org/apache/commons/math3/stat/descriptive/UnivariateStatistic.html),
case insensitive.
All those provided with Apache Commons Math are available by default.
New statistics can be defined,
they get loaded transparently as far as their package matches the one of Apache Commons Math.

If the aggregators are specified, only one value per aggregator gets exported,
instead of one value for each node.

### `extractor.value-filter`

**Type**: String or SpecMap

Builds a {{% api package="loader.export" class="FilteringPolicy" %}},
to be applied to raw data before being processed by the `aggregators`(#extractoraggregators),
if present.
If a String is provided, then it is used to load a policy from {{% api package="loader.export.filters" class="CommonFilters" %}}.
Otherwise, the [arbitrary class loading system](#arbitrary-class-loading-system) **MUST** be used.


| Mandatory keys | Optional keys                             |
|----------------|-------------------------------------------|
| `type`         | `parameters`                              |

### `extractor.value-filter.type`

Same as [type](#type)

### `extractor.value-filter.parameters`

Same as [parameters](#parameters)

---

### `layer`

**Type**: SpecMap

Builds a {{% api class="Layer" %}}
using the [arbitrary class loading system](#arbitrary-class-loading-system).

#### Examples

* Creation of two {{% api class="Layer" %}}s
    {{< code path="alchemist-loading/src/test/resources/synthetic/testlayer.yml" >}}
* Creation of two {{% api package="model.implementations.layers" class="BidimensionalGaussianLayer" %}}s:
    {{< code path="alchemist-cognitive-agents/src/test/resources/social-contagion.yml" >}}

---

### `layers`

**Type**: Traversable of [`layer`](#layer)

#### Examples

* Creation of two {{% api class="Layer" %}}s
    {{< code path="alchemist-loading/src/test/resources/synthetic/testlayer.yml" >}}
* Creation of two {{% api package="model.implementations.layers" class="BidimensionalGaussianLayer" %}}s:
    {{< code path="alchemist-cognitive-agents/src/test/resources/social-contagion.yml" >}}

---

### `network-model`

**Type**: SpecMap

Builds a {{% api class="LinkingRule" %}}
using the [arbitrary class loading system](#arbitrary-class-loading-system).
If unspecified, defaults to {{% api package="model.implementations.linkingrules" class="NoLinks" %}},
and no nodes will have any neighbor.

#### Examples
* Nodes connected when closer than some range
  {{<code path="src/test/resources/website-snippets/deployment-in-three-points.yml" >}}

---

### `program`

**Type**: SpecMap

Definition of the contents ({{% api class="Molecule" %}}s and {{% api class="Concentration" %}}s) of a group of nodes.

**(Multi)Spec**

| Mandatory keys | Optional keys                                             |
|----------------|-----------------------------------------------------------|
| `type`         | `parameters`, `conditions`, `time-distribution` `actions` |
| `program`      | `time-distribution`                                       |

### `program.type`

Same as [type](#type)

### `program.program`

**Type**: String

Passed to {{% api class="Incarnation" method="createReaction" %}} to be interepreted and

### `program.in`

**Type**: Traversable of [shapeFilter](#shapefilter)

### `program.actions`

**Type**: Traversable of [`action`](#action)

### `program.conditions`

**Type**: Traversable of [`condition`](#condition)

### `program.parameters`

Same as [parameters](#parameters)

### `program.time-distribution`

**Type**: String or SpecMap

Builds a {{% api class="TimeDistribution" %}}.
If a String is provided, then it is created via {{% api class="Incarnation" method="createTimeDistribution" %}}.
Otherwise, the [arbitrary class loading system](#arbitrary-class-loading-system) **SHOULD** be used.

---

### `remote-dependencies`

---

### `shapeFilter`

**Type**: SpecMap

Builds a {{% api package="loader.filters" class="Filter" %}}
using the [arbitrary class loading system](#arbitrary-class-loading-system).

#### Examples
* Injection of a molecule only in those nodes located inside a {{% api package="loader.filters" class="Rectangle" %}}
  {{<code path="src/test/resources/website-snippets/grid-dodgeball.yml" >}}

---

### `seeds`

**Type**: SpecMap

Selection of the seed for the
[`RandomGenerator`]()s.

**(Multi)Spec**

| Mandatory keys | Optional keys            |
|----------------|--------------------------|
|                | `scenario`, `simulation` |

### `seeds.scenario`

**Type**: Int

Selection of the seed for the
[`RandomGenerator`](https://javadoc.io/static/org.apache.commons/commons-math3/3.6.1/org/apache/commons/math3/random/RandomGenerator.html)
controlling the position of random displacements.

### `seeds.simulation`

**Type**: Int

Selection of the seed for the
[`RandomGenerator`](https://javadoc.io/static/org.apache.commons/commons-math3/3.6.1/org/apache/commons/math3/random/RandomGenerator.html)
controlling the evolution of the events of the simulation.


---

### `terminate`

**Type**: Traversable of [`terminator`](#terminator)

---

### `terminator`

**Type**: SpecMap

Builds a [`Predicate`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Predicate.html)
using the [arbitrary class loading system](#arbitrary-class-loading-system).

#### Examples
* Termination after some time
  {{<code path="alchemist-implementationbase/src/test/resources/termination.yml" >}}
  {{<code path="alchemist-loading/src/test/resources/testCSVExporter.yml" >}}
  {{<code path="alchemist-loading/src/test/resources/testMongoExporter.yml" >}}


### `variable`

**Type**: SpecMap

Definition of [free](/howtos/simulation/variables/#free-variables)
and [dependent](/howtos/simulation/variables/#dependent-variables) variables.

**(Multi)Spec**

| Mandatory keys                  | Optional keys         |
|---------------------------------|-----------------------|
| `type`                          | `parameters`          |
| `min`, `max`, `step`, `default` |                       |
| `formula`                       | `language`, `timeout` |

Variables can be created in three ways:
* Using the [arbitrary class loading system](#arbitrary-class-loading-system)
  to produce an instance of {{% api package="loader.variables" class="Variable" %}} or
  {{% api package="loader.variables" class="DependentVariable" %}};
* specifying the parameters of a {{% api package="loader.variables" class="LinearVariable" %}}
  (minimum and maximum values, incrementation step, and default value);
* writing an expression that can be interpreted by some JSR-223-compatible language whose interpreter is in the
  classpath, possibly specifying a timeout. Produces a {{% api package="loader.variables" class="DependentVariable" %}}.

### `variable.type`

Same as [type](#type)

### `variable.parameters`

Same as [parameters](#parameters)

### `variable.default`

**Type**: Number

Default value for a {{% api package="loader.variables" class="LinearVariable" %}},
to be selected if the variable is not among those generating the batch.

### `variable.max`

**Type**: Number

Maximum value for a {{% api package="loader.variables" class="LinearVariable" %}}

### `variable.min`

**Type**: Number

Minimum value for a {{% api package="loader.variables" class="LinearVariable" %}}

### `variable.step`

**Type**: Number

Size of the incremental step of a {{% api package="loader.variables" class="LinearVariable" %}}

### `variable.formula`

**Type**: String

Code that can be interpreted by a {{% api package="loader.variables" class="JSR223Variable" %}}.

### `variable.language`

**Type**: String

Language to be used by a {{% api package="loader.variables" class="JSR223Variable" %}}.
The language must be available in the classpath.
Groovy (default), Kotlin (`kotlin` or `kts`), and Scala (`scala`) are supported natively.

### `variable.timeout`

**Type**: Int

Time in milliseconds after which the interpreter of the
{{% api package="loader.variables" class="JSR223Variable" %}}
is considered stuck or in livelock.
The interpreter gets interrupted and the simulation loading fails to prevent unresponsive simulations.
Defaults to 1000ms.

---

### `variables`

**Type**: Traversable of [`variable`](#layer)

---
