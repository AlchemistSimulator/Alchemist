---

title: "Basics of an Alchemist simulation"

---

## Layers

It is possible to define overlays (layers) of data that can be sensed everywhere in the environment.
Layers can be used to model physical properties, such as pollution, light, temperature, and so on.
Conversely than readings from nodes' contents, layers have no dependency optimization.
This implies that reactions that read values from layers should have special care in defining their `context` appropriately

In order to create layer, the programmer must define the type of the layer, a molecule that will be used as identifier,
and possibly the parameters needed for intializing the type of layer you have chosen, as per the [`type/parameter` syntax](#loading-arbitrary-java-classes-with-the-typeparameters-syntax).

{{% code path="alchemist-loading/src/test/resources/synthetic/testlayer.yml" %}}

The following example exemplifies the syntax for initializing two {{ anchor('BidimensionalGaussianLayer') }}: 

{{% code path="alchemist-cognitive-agents/src/test/resources/social-contagion.yml" %}}


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

