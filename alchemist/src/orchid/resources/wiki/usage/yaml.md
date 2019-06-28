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

### Dependent variables

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
        molecule: randomSensor
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

## Writing layers

In order to put a layer inside the previously specified environment you have to define the type of the layer, the molecule
it refers to and possibly the parameters needed for the type of layer you have chosen.  
Of course, it is possible to add more than one layer inside the same environment.  
The syntax is like the following:  

```yaml
layers:
  - type: BidimensionalGaussianLayer
    molecule: pippo
    parameters: [0.0, 0.0, 2.0, 5.0]
  - type: BidimensionalGaussianLayer
    molecule: pluto
    parameters: [0.0, 0.0, 5.0, 10.0]
```