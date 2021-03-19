---

title: "Basics of an Alchemist simulation"

---

As a first step, we recommend learning the YAML basics.
The language is so simple and human readable that there is probably no better way to learn it than to read it directly.
My suggestion is to use the tutorial "[Learn X in Y minutes where X = YAML](https://learnxinyminutes.com/docs/yaml/)",
it should provide a good YAML guide (surely sufficient to follow the tutorial).

Alchemist expects a YAML map as input. In the following section, we'll discuss which keys it expects.
Of course, users are free to use all the YAML features (e.g. anchors and merge keys)
to organize their code and reduce duplication.

## Choosing an incarnation

The `incarnation` key is mandatory.
Actually, it's the only mandatory key.
The YAML parser expects a string value.
Such string will be used to get the most similarly named incarnation, namely the subclass of Incarnation whose simple
name is closest to the string.

**Examples**

{{ snippet("just-loading/minimal-sapere.yml") }}

{{ snippet("just-loading/minimal-protelis.yml") }}

{{ snippet("just-loading/minimal-biochemistry.yml") }}

*Note:* this is also the most minimal valid alchemist specification

## Loading arbitrary Java classes with the `type`/`parameters` syntax

Alchemist YAML lets the user control which actual Java classes should be
loaded inside a simulation, and which constructor should be used to do so.
Almost every entity of an Alchemist simulation can be instanced through arbitrary Java classes implementing the required interfaces.
When the alchemist parser encounters a map providing the keys `type` and `parameters`, it tries to resolve the
value of the value associated to `type` to a class name, then tries to create the object by
searching a constructor compatible with the parameters known by the context plus those provided in `parameters`.

**Class name resolution**

The value associated with `type` must be a string representing a valid Java identifier.
If the value contains one or more `.` characters, then it will be interpreted as a fully qualified name.
If no such character is included, then it will be interpreted as a simple name.
Multiple classes with a common supertype and the same simple name may conflict, in which case the simulator raises an error.

**Object instancing**

If the class gets loaded correctly, then its constructors get sorted based on the number and type of parameters.
The system tries to build an object with all the available constructors until one of them provides an instanced object,
in an order that considers both the current context (namely, the entities that have already been instanced) and the
value of `parameters`.

For instance, imagine that you are trying to build an instance of a {{ anchor('Reaction') }}, whose only constructor requires an
{{ anchor('Environment') }}, a {{ anchor('Node') }}, an `int` and a `String`.
In this case, an Environment and a Node must have already been created
(or the simulation loading process would have failed already).
As a consequence, the first two parameters are automatically inferred by the current context and passed to the constructor.
The other two parameters can not be inferred this way;
instead, the value associated to `parameters` is used to extract the proper values (if possible).
In this case, this would have been a valid `parameters` entry:

```yaml
type: my.package.MyCustomReaction
parameters: [4, foo]
```

As you can easily infer, the value of `parameters` must be a YAML list.

Don't despair if the class loading system is still unclear: it will become clearer with the examples in the forthcoming sections.

## Setting up the environment

The `environment` key is used to load the {{ anchor('Environment') }} implementation.
It is optional and it defaults to a {{ anchor('continuous bidimensional space', 'Continuous2DEnvironment') }}.

### Examples

The following simulations are equivalent, and load the default environment (which is incarnation independent, here
`protelis` is picked, but it works for any other incarnation as well):

{{ snippet("just-loading/minimal-protelis.yml") }}

{{ snippet("just-loading/envtype-protelis.yml") }}

{{ snippet("just-loading/envtype-fullyqualified-protelis.yml") }}

{{ snippet("just-loading/envtype-explicitparameters-protelis.yml") }}

Alchemist supports
{{ anchor('real world maps', 'Maps and GPS traces') }}
and 
{{ anchor('indoor environments with obstacles', 'Indoor environments') }}.

Of course, in case you have a custom {{ anchor('Environment') }} implementation,
you can load it using the `type`/`parameters` syntax.

The environments shipped with the distribution can be found in the package
{{ anchor('it.unibo.alchemist.model.implementations.environments') }}.

## Displacing nodes

Once the environment is set up, it is time to populate it with nodes.
The `displacements` section lists the node locations at the beginning of the simulation.
Each displacement type extends the interface {{ anchor('Displacement') }}.

### Displacing on specific positions

The following example places a single node in the (0, 0) {{ anchor('Point') }}.
{{ snippet("has-nodes/diplacements-in-point.yml") }}

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
  - type: Circle
    parameters: [10000, 0, 0, 10]
```

Here instead nodes are located in a {{ anchor('Grid') }} centered in (0, 0), with nodes distanced of 0.25 both
horizontally and vertically, and whose position is not exact but randomly perturbed (±0.1 distance units).
```yaml
displacements:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
```

### Customizing the node type

In order to specify a particular node implementation you want to put inside the environment you can use the `nodes` key
followed by the name of the class and the parameters required to build it.

100 `MyCustomNodeImpl` nodes, whose constructor needs only the environment, placed in a circle with center in (0, 0) and
radius 20.
```yaml
displacements:
  - type: Circle
    parameters: [100, 0, 0, 20]
    nodes:
      type: MyCustomNodeImpl
      parameters: []
```

The empty parameters section can be omitted (as per custom class loading mechanism):
```yaml
displacements:
  - type: Circle
    parameters: [100, 0, 0, 20]
    nodes:
      type: MyCustomNodeImpl
```

### Customizing the nodes content

It is possible to set the content of the nodes in a deployment.
Node contents are defined in terms of molecules and their corresponding concentration.
As such, they depend on the specific incarnation in use.

In the following example, we inject in all the nodes of a {{ anchor('Grid') }} deployment a molecule called `foo`  with
concentration `1`.
As stated before, it would only make sense if the incarnation supports integer concentrations and it's able to produce
a valid molecule from the `"foo"` String.

```yaml
displacements:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - molecule: foo
        concentration: 1
```

Multiple contents can be listed, e.g.,
if we want to also have a molecule named `bar` with value `0` along with `foo`,
we can just add another entry to the list:

```yaml
displacements:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - molecule: foo
        concentration: 1
      - molecule: bar
        concentration: 0
```

Molecules can be injected selectively inside a given {{ anchor('Shape') }}.
To do so, you can a filter with the `in keyword`.
In the following example, only the nodes inside the {{ anchor('Rectangle') }} area contain
the `source` molecule.

```yaml
displacements:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - in:
          type: Rectangle
          parameters: [-6, -6, 2, 2]
        molecule: source
        concentration: true
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

## Writing behaviors (Reactions)

Nodes can be programmed using reactions.
Reaction are usually highly dependent on the specific incarnation.

```yaml
# Variable representing the program to be executed
gradient: &gradient
  - time-distribution: 1
    # Make sure that the program folder is part of the project classpath
    program: program:package:distanceTo
  - program: send
displacements:
  - type: Grid
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

## Layers

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

## Terminating the simulation if a condition is met

Alchemist supports the possibility to write termination conditions for any simulation.
Termination conditions are checked after every event, and, if met, cause the immediate termination of a simulation.
Termination conditions are expected to be found in the {{ anchor('it.unibo.alchemist.model.implementations.terminators') }} package.

To load them, use the `terminators` keyword.
Multiple terminators are allowed, the first terminator matching causes the termination of the simulation (they are in and).

Here is an example:
```yaml
terminate:
  # Defines a new terminator which every 100 simulation steps for the environment to remain equal for the 10 subsequent
  # simulation steps. If no change is detected, then the simulation is intended as concluded.
  - type: StableForSteps
    parameters: [100, 10]
```

### Terminating the simulation after some time

One of the simplest terminators availables allows for declaring a simulation completed when a certain simulated time is reached.
In the following example, it is used in conjunction with a number of variables, showing how it's possible to use such
variables to produce batches of simulations terminating at different times.

```yaml
variables:
  stabilizationTime:
    type: ArbitraryVariable
    parameters: [10, [0, 1, 10, 100, 1000]]
  simulationEnd: &simulationEnd
    formula: 150 + stabilizationTime
terminate:
  - type: AfterTime
    parameters: [*simulationEnd]
```

### Terminating the simulation if the environment is not changing

A terminator is provided for terminating when a simulation is "stable" (nothing changes in terms of positions and nodes' content).
The class implementing it is {{ anchor('StableForSteps') }}.
The following code snippet exemplifies its usage:
```yaml
terminate:
  # Defines a new terminator which every 100 simulation steps for the environment to remain equal for the 10 subsequent
  # simulation steps. If no change is detected, then the simulation is intended as concluded.
  - type: StableForSteps
    parameters: [100, 10]
```

## Controlling the reproducibility

Alchemist simulations can be reproduced by feeding them the same random number generator.
This assumption is true as far as the custom component in use:
* do not use any other random generators but the one provided by the simulation framework (all the standard components are guaranteed to do so);
* do not iterate over collections with no predicible iteration order (i.e., `Set` and `Map`) containing elements (or
  keys) whose `hashCode()` has not been overridden to return the same value regardless of the specific JVM in use;
* do not run operations in parallel.

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

