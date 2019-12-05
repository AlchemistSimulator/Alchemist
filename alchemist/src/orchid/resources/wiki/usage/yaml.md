---

title: "Writing Alchemist simulations"

---

As a first step, we recommend learning the YAML basics.
The language is so simple and human readable that there is probably no better way to learn it than to read it directly.
My suggestion is to use the tutorial "[Learn X in Y minutes where X = YAML](https://learnxinyminutes.com/docs/yaml/)",
it should provide a good YAML guide (surely sufficient to follow the tutorial).

Alchemist expects a YAML map as input. In the following section, we'll discuss which keys it expects.
Of course, users are free to use all the YAML features (e.g. anchors) to organize their code and reduce duplication.

## Choosing an incarnation

The `incarnation` key is mandatory.
Actually, it's the only mandatory key.
The YAML parser expects a string value.
Such string will be used to get the most similarly named incarnation, namely the subclass of Incarnation whose simple
name is closest to the string.

**Examples**

```yaml
incarnation: sapere
```

```yaml
incarnation: protelis
```

```yaml
incarnation: biochemistry
```

*Note:* this is also the most minimal valid alchemist specification

## Loading arbitrary Java classes with the `type`/`parameters` syntax

One important aspect of the Alchemist YAML is the ability to let the user control which actual Java classes should be
loaded inside a simulation, and which constructor should be used to do so.
Almost every entity of an Alchemist simulation can be instanced using arbitrary Java classes that implement the required
interfaces.
When the alchemist YAML parser encounters a YAML Map providing the keys `type` and `parameters`, it tries to resolve the
value of the value associated to `type` to a class name, then tries to create the object by calling the constructor with
parameters most suited to the value of `parameters`.

**Class name resolution**

The value associated with `type` must be a string representing a valid Java identifier.
If the value contains one or more `.` characters, then it will be interpreted as a fully qualified name.
If no such character is included, then *the default package for the desired alchemist entity will be prefixed*.
Alchemist won't ever attempt to load a class situated in the default package (so don't put your classes there, it's a
bad practice anyway).

**Object instancing**

If the class gets loaded correctly (meaning if a class is present in the classpath with the fully qualified name,
whether it was passed or guessed by Alchemist), then its constructors get sorted based on the number and type of
parameters.
The system tries to build an object with all the available constructors until one of them provides an instanced object,
in an order that considers both the current context (namely, the entities that have already been instanced) and the
value of `parameters`.

For instance, imagine that you are trying to build an instance of a {{ anchor('Reaction') }}, whose only constructor requires an
{{ anchor('Environment') }}, a {{ anchor('Node') }}, an `int` and a `String`.
In this case, an Environment and a Node must have already been created (or the YAML loader won't be at this point).
As a consequence, the first two parameters are automatically inferred by the current context and passed to the constructor.
The other two parameters can not be inferred this way; instead, the value associated to `parameters` is used to extract the proper values (if possible).
In this case, this would have been a valid `parameters` entry:

```yaml
type: my.package.MyCustomReaction
parameters: [4, foo]
```

As you can easily infer, the value of `parameters` must be a YAML list.

Don't despair if the class loading system is still unclear: it is used pervasively and it will become clearer with the examples in the next sections.

## Setting up the environment

The `environment` key is used to load the {{ anchor('Environment') }} implementation.
It is optional and it defaults to a {{ anchor('continuous bidimensional space', 'Continuous2DEnvironment') }}.
If no fully qualified environment name is provided for class loading, Alchemist uses the package
{{ anchor('it.unibo.alchemist.model.implementations.environments') }} to search for the class.

### Examples

The following simulations are equivalent, and load the default environment (which is incarnation independent, here
`protelis` is picked, but it works for any other incarnation as well):
```yaml
incarnation: protelis
```
```yaml
incarnation: protelis
environment:
  type: Continuous2DEnvironment
```
```yaml
incarnation: protelis
environment:
  type: it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
```
```yaml
incarnation: protelis
environment:
  type: Continuous2DEnvironment
  parameters: []
```

{{ anchor('OSMEnvironment') }} allows for running simulations over real world maps. The following simulation
loads data from an Openstreetmap file (OSM, XML and PBF formats are supported) located in the classpath in the folder
`maps`:
```yaml
incarnation: protelis
environment:
  type: OSMEnvironment
  parameters: [maps/foo.pbf]
```

{{ anchor('ImageEnvironment') }} loads data from a black and white raster image file (in this example, located in the
classpath in the folder `images`), interpreting the black pixels as obstacles (areas that cannot be accessed by nodes):
```yaml
incarnation: protelis
environment:
  type: ImageEnvironment
  parameters: [images/foo.png]
```

Finally, if you write your own custom class named `my.package.FooEnv` implementing {{ anchor('Environment') }}, whose
constructor requires a String and a double, you can use it in the simulator by writing, for instance:
```yaml
incarnation: protelis
environment:
  type: my.package.FooEnv
  parameters: [bar, 2.2]
```

The environments shipped with the distribution can be found in the package
{{ anchor('it.unibo.alchemist.model.implementations.environments') }}.

## Declaring variables

The `variables` section lists variable simulation values.
A variable defines some kind of value that can be referenced in the simulation file.

There are two kinds of variables: free and dependent variables.
The difference is that variables of the latter kind can always be computed given the values of all the other variables.

### Free variables

Free variables define a set of values and a default.
The value set is used to define the batch matrix, i.e., if the variable is selected for a batch, all its value are
included in the cartesian product that determines which simulations are to be executed.
If the simulation is not executed as batch, then the default value is used

#### Linear variables

#### Geometric variables

#### Arbitrary-valued variables

```yaml
variables:
  myvar: &myvar
    type: ArbitraryVariable
    parameters: ["defaultValue", ["value1","value2","value3"]]
```

### Dependent variables

Some variables are combination of free parameters.
Let's suppose that we want to deploy on a circle, but for some reason (e.g. because it is required by the constructor of some action) we need to compute and have available radius and perimeter.
We don't need to control both of them: the perimeter can be computed.
Alchemist provides support for performing computation over variables.
Let's first define our radius.
We want it to be a free variable, ranging geometrically from 0.1 to 10 in nine steps, and defaulting to 1.
```yaml
variables:
  radius: &radius
    type: GeometricVariable
    parameters: [1, 0.1, 10, 9]
```
Now we want to compute the diameter.
We can do so by using the `formula` syntax:
```yaml
variables:
  radius: &radius
    type: GeometricVariable
    parameters: [1, 0.1, 10, 9]
  diameter: &diam
    formula: Math.PI * 2 * radius
```
How does it work?
Alchemist feeds the formula String to an interpreter and takes the result of the interpretation.
By default, [Groovy](https://groovy-lang.org/) is used as language to interpret the formula, but other languages can be used.

Variables can be defined in any order.
Alchemist figures out the dependencies automatically, as far as there are no cyclic dependencies (e.g. variable `a` requires `b`, and `b` requires `a`).
Please note that the simulator variable dependency resolution system is not designed to solve mathematical systems,
so even though the problem has a well formed mathematical solution the actual variable resolution may fail;
e.g. if `a` is defined as `2 * b + 1`, and `b` is defined as `4 - a`, the system **won't** bind `a` to `3` and `b` to `1`,
but will simply fail complaining about circular dependencies.

### Using different languages

In order to use a language different than Groovy, the user may specify it explicitly by using the `language` keyword.
For example, Scala can be used:
```yaml
variables:
  radius: &radius
    type: GeometricVariable
    parameters: [1, 0.1, 10, 9]
  diameter: &diam
    formula: Math.PI * 2 * radius
    language: scala
```
Or Kotlin:
```yaml
variables:
  radius: &radius
    type: GeometricVariable
    parameters: [1, 0.1, 10, 9]
  diameter: &diam
    formula: listOf(Math.PI, 2.0, 0.3).fold(1.0) { a, b -> a * b  }
    language: kotlin
```

The system is [JSR-233](http://archive.fo/PGdk8)-compatible, as such, every language with a valid JSR-233 implementation could be used.
The only requirement for the language to be available is the availability in the runtime classpath of a JSR-233 compatible version of the desired language.
If Alchemist is being used (as recommended) in conjunction with Gradle, and you want to embed your favorite JSR-233 compatible scripting language, you should have a dependency declaration similar to:

```kotlin
dependencies {
    ...
    runtimeOnly("my.favorite.scripting.language:supporting-jsr233:0.1.0")
    ...
}
``` 

For instance, Alchemist supports Kotlin and Groovy natively by simply providing in its `build.gradle.kts` something similar to:
```kotlin
dependencies {
    ...
    runtimeOnly("org.codehaus.groovy:groovy-jsr223:2.5.7")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223-embeddable:1.3.40")
    runtimeOnly("org.jetbrains.kotlin:kotlin-script-runtime:1.3.40")
    runtimeOnly("org.jetbrains.kotlin:kotlin-script-util:1.3.40")}
    ...
``` 
Alchemist provides a number of ready-to use interpreters. Besides Groovy (used by default) it includes:

* [Scala](https://www.scala-lang.org/)
* [Kotlin](https://kotlinlang.org/)

Moreover, several implementations of the Java Virtual Machine feature internal interpreters for
[ECMAScript](https://www.ecma-international.org/publications/standards/Ecma-262.htm)/
[Javascript](https://en.wikipedia.org/wiki/JavaScript).
In case they are provided, such engines can be used without any additional effort.
Javascript used to be the default for Alchemist, but it has been replaced by Groovy since
[Nashorn, the interpreter embedded in OpenJDK, is deprecated](https://openjdk.java.net/jeps/335).


#### Multiline programs

Sometimes data manipulation can get tricky and trivial scripting may no longer be enough.
In such cases, and especially with modern languages that allow for a reduced usage of cerimonial semicolons (such as Kotlin and Scala), it can be useful to write multiline programs.
This can be achieved in YAML by using the pipe `|` operator, as exemplified in the following snippet:

```yaml
variables:
  a:
    formula: 22 + 1
    language: kt
  test:
    formula: |
      import com.google.common.reflect.TypeToken
      import com.google.gson.Gson
      Gson().fromJson<Map<String, List<List<List<Double>>>>>(
          ClassLoader.getSystemResourceAsStream("explorable-area.json")?.reader(),
          object: TypeToken<Map<String, List<List<List<Double>>>>>() {}.type
      )
      .get("coordinates")!!
      .first()
      .map { Pair(it.last(), it.first()) }
    language: kotlin
```
If the string begins with a `|`, its contents preserve newlines, thus allowing for multiline scripts of arbitrary complexity.

#### Known issues

Alchemist exploit JSR-233's variable binding system to let the scripts use variables defined elsewhere.
Not all languages support this system properly.
In particular, Kotlin does not (yet) support variable injection and requires a workaround.
In order for a script to access a variable named `myVar`, the programmer should write instead `bindings["myVar"]`.
The issue is being tracked as [KT-15125](https://youtrack.jetbrains.com/issue/KT-15125).
Once it gets solved (if ever), and as soon as Alchemist incorporates the version of Kotlin including the fix,
the workaround will no longer be necessary.

### Using variables

## Controlling the reproducibility

Alchemist simulations can be reproduced by feeding them the same random number generator.
This assumption is true as far as the custom component in use:
* do not use any other random generator but the one provided by the simulation framework
* do not iterate over collections with no predicible iteration order (i.e., `Set` and `Map`) containing elements (or
keys) whose `hashCode()` has not been overridden to return the same value regardless of the specific JVM in use.
* do not run operations in parallel

The `seeds` section may contain two optional values: `scenario` and `simulation`.
The former is the seed of the pseudo-random generator used during the creation of the simulation, e.g. for displacing
nodes in random arrangements.
The latter is the seed of the pseudo-random generator used during the simulation, e.g. for computing time distributions
or generating random positions.
A typical example in which one may want to have different values, is to keep the same random displacement of devices in
some scenario but allow events to happen with different timings.

A typical `seed` section may look like:

```yaml
seeds:
  scenario: 0
  simulation: 1
```

Usually, in batches, you wan to run multiple runs per experiment, varying the simulation seed, in order to get more
reliable data (and appropriate error bars).
As per any other value, variables can be feeded as random generator seeds.
In the following example, 100 simulations are generated with different seeds (both for environment configuration and
simulation execution)

```yaml
variables:
  random: &random
    min: 0
    max: 9
    step: 1
    default: 0
seeds:
  # reference to the `random` variable
  scenario: *random
  simulation: *random
```

## Defining the network

The `network-model` key is used to load the implementation of {{ anchor('LinkingRule') }} to be used in the simulation,
which determines the neighborhood of every node.
The key is optional, but defaults to {{ anchor('NoLinks') }}, so if unspecified nodes in the environment don't get
connected.
Omitting such key is equivalent to writing any of the following:
```yaml
network-model:
  type: NoLinks
```
```yaml
network-model:
  type: it.unibo.alchemist.model.implementations.linkingrules.NoLinks
  parameters: []
```
```yaml
network-model:
  type: NoLinks
  parameters: []
```
If no fully qualified linking rule name is provided for class loading, Alchemist uses the package
{{ anchor('it.unibo.alchemist.model.implementations.linkingrules') }} to search for the class.

### Linking nodes based on distance

One of the most common ways of linking nodes is to connect those which are close enough to each other. To do so, you can
use the class {{ anchor('ConnectWithinDistance') }}, passing a parameter representing the maximum connection distance.
Note that such distance depends on the environment: while the definition of distance is straightforward for euclidean
spaces, it's not so for [Riemannian manifolds](https://en.wikipedia.org/wiki/Riemannian_geometry), which is a fancy
name to define geometries such as the one typical of a urban map (you can roughly interpret it as a euclidean space
"with holes").
For instance, in case of environments using {{ anchor('GeoPosition') }}, the distance is computed in meters, so the
distance between `[44.133254, 12.237770]` and `[44.146680, 12.258627]` is about `2240` (meters).

```yaml
network-model:
  type: ConnectWithinDistance
  # Link together all the nodes closer than 100 according to the distance function
  parameters: [100]
```

## Displacing nodes

The `displacements` section lists the node locations at the beginning of the simulation.
Each displacement type extends the interface {{ anchor('Displacement') }}. If no fully qualified displacement name is
provided for class loading,
Alchemist uses the package {{ anchor('it.unibo.alchemist.loader.displacements') }} to search for the class.
The YAML key associated to displacements is `in`.

### Displacing on specific positions

The following example places a single node in the (0, 0) {{ anchor('Point') }}.
```yaml
displacements:
  # "in" entries, where each entry defines a group of nodes
  - in:
      type: Point
      # Using a constructor taking (x,y) coordinates
      parameters: [0, 0]
```

### Displacing multiple nodes on specific positions

```yaml
displacements:
  - in:
      type: SpecificPositions
      parameters: [[0,1],[2,2],[3,4]]
```

### Displacing multiple nodes at once

This example places 10000 nodes randomly in a {{ anchor('Circle') }} with center in (0, 0) and radius 10.
```yaml
displacements:
  - in:
      type: Circle
      parameters: [10000, 0, 0, 10]
```

Here instead nodes are located in a {{ anchor('Grid') }} centered in (0, 0), with nodes distanced of 0.25 both
horizontally and vertically, and whose position is not exact but randomly perturbed (±0.1 distance units).
```yaml
displacements:
  - in:
      type: Grid
      parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
```

### Customizing the node type

In order to specify a particular node implementation you want to put inside the environment you can use the `nodes` key
followed by the name of the class and the parameters required to build it.

100 `MyCustomNodeImpl` nodes, whose constructor needs only the environment, placed in a circle with center in (0, 0) and
radius 20.
```yaml
displacements:
  - in:
      type: Circle
      parameters: [100, 0, 0, 20]
    nodes:
      type: MyCustomNodeImpl
      parameters: []
```

The empty parameters section can be omitted (as per custom class loading mechanism):
```yaml
displacements:
  - in:
      type: Circle
      parameters: [100, 0, 0, 20]
    nodes:
      type: MyCustomNodeImpl
```

### Customizing the nodes content

It is possible to set the content of the nodes inside a given region. Only the nodes inside the {{ anchor('Rectangle') }} area contain
the `source` and `randomSensor` molecules (global variables).
```yaml
displacements:
  - in:
      type: Grid
      parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - in:
          type: Rectangle
          parameters: [-6, -6, 2, 2]
        molecule: source
        concentration: true
      - molecule: randomSensor
        concentration: >
          import java.lang.Math.random
          random() * pi
```

## Writing behaviors (Reactions)

TODO!

Nodes can be programmed using reactions.

```yaml
# Variable representing the program to be executed
gradient: &gradient
  - time-distribution: 1
    # Make sure that the program folder is part of the project classpath
    program: program:package:distanceTo
  - program: send
displacements:
  - in:
      type: Grid
      parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    programs:
      # Reference to the "gradient" list of programs. This program is executed in all
      # the grid nodes
      - *gradient
```

### Triggers

```yaml
pools:
  - pool:
      - time-distribution:
          type: Trigger
          parameters: [0] # the param is the time step
      type: Event
      actions:
        - type: MyActionType
          parameters: [...] #
```

## Writing layers

It is possible to define overlays (layers) of data that can be sensed everywhere in the environment.
Layers can be used to model physical properties, such as pollution, light, temperature, and so on.
Conversely than readings from nodes' contents, layers have no dependency optimization.
This implies that reactions that read values from layers should have special care in defining their `context` appropriately

In order to create layer, the programmer must define the type of the layer, a molecule that will be used as identifier,
and possibly the parameters needed for intializing the type of layer you have chosen, as per the [`type/parameter` syntax](#loading-arbitrary-java-classes-with-the-typeparameters-syntax).

The following example exemplifies the syntax for initializing two {{ anchor('BidimensionalGaussianLayer') }}: 

```yaml
layers:
  - type: BidimensionalGaussianLayer
    molecule: foo
    parameters: [0.0, 0.0, 2.0, 5.0]
  - type: BidimensionalGaussianLayer
    molecule: bar
    parameters: [0.0, 0.0, 5.0, 10.0]
```