---

title: "Basics of an Alchemist simulation"

---

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

