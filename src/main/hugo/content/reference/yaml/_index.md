+++
pre = ""
title = "YAML simulation specification"
weight = 2
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

| Type                  | Description                                                                                                                                                                                        |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| List                  | Any YAML List                                                                                                                                                                                      |
| Map                   | Any YAML Map                                                                                                                                                                                       |
| SpecMap               | A YAML Map matching a MultiSpec                                                                                                                                                                    |
| Spec                  | Pair of lists of strings. The first list contains mandatory keys, the second optional keys. A map matches a Spec if it contains all its mandatory keys, any of the optional keys, and no other key |
| MultiSpec             | A list of Spec. A Map matches a MultiSpec if it matches one and only one of its Spec.                                                                                                              |
| String                | YAML String                                                                                                                                                                                        |
| Traversable           | One of: A SpecMap, a List of Traversable, a Map of Traversable                                                                                                                                     |

---

### Arbitrary class loading system

**Type**: SpecMap

Alchemist is able to load arbitrary types conforming to the expected `interface`
(or Scala `trait`).

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
{{% api package="loader" class="Deployment" %}},
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

Instantiation is delegated to the [Java Implicit Reflective Factory](https://github.com/DanySK/jirf/).

#### Examples

* Construction of a {{% api package="loader.deployments" class="Point" %}}
    {{<code path="src/test/resources/website-snippets/deployment-in-point.yml" >}}
* Construction of variables with named parameters
    {{<code path="src/test/resources/website-snippets/named-parameters.yml" >}}


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

### `deployments`

**Type**: Traversable

Traversable of [`deployment`](#deployment)

### `deployment`

**Type**: SpecMap

Definition of the positions of a set of nodes.
Builds a 
{{% api package="loader" class="Deployment" %}}.

**(Multi)Spec**

| Mandatory keys | Optional keys                                 |
|----------------|-----------------------------------------------|
| `type`         | `parameters`, `contents`, `nodes`, `programs` |

### `deployment.type`

Same as [type](#type)

### `deployment.parameters`

Same as [parameters](#parameters)

### `deployment.contents`

### `deployment.nodes`

### `deployment.programs`

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

### `environment`

Same as [arbitrary class loading system](#arbitrary-class-loading-system).

If left unspecified, defaults to a bidimensional Euclidean manifold:
{{% api package="model.implementations.environments" class="Continuous2DEnvironment" %}}.

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

---

### `layers`

---

### `network-model`

---

### `remote-dependencies`

---

### `seeds`

---

### `terminate`

---

### `variables`

