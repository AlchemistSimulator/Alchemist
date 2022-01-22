---

title: "Basics of an Alchemist simulation"

---

### Customizing the nodes' content

It is possible to set the content of the nodes in a deployment.
Node contents are defined in terms of molecules and their corresponding concentration.
As such, they depend on the specific incarnation in use.

In the following example, we inject in all the nodes of a {{ anchor('Grid') }} deployment a molecule called `foo`  with
concentration `1`.
As stated before, it would only make sense if the incarnation supports integer concentrations and it's able to produce
a valid molecule from the `"foo"` String.

```yaml
deployments:
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
deployments:
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
deployments:
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
deployments:
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
The former is the seed of the pseudo-random generator used during the creation of the simulation, e.g. for deploying
nodes in random arrangements.
The latter is the seed of the pseudo-random generator used during the simulation, e.g. for computing time distributions
or generating random positions.
A typical example in which one may want to have different values, is to keep the same random deployment of devices in
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

