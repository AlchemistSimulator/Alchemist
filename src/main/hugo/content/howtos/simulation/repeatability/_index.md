+++
title = "Ensure repeatability"
weight = 1
tags = ["randomness", "reproducibility", "replicability", "seed", "random"]
summary = "Control randomness, ensuring reproducibility and replicability of experiments."
+++

*Debugging* a simulation requires the ability to reproduce the same behavior multiple times:
an unexpected behavior requiring investigation may happen far into the simulation,
or in corner conditions encountered by chance.
Randomness is controlled by setting the random generator seeds *separately for the deployments and the simulation execution*,
allowing for running different simulations on the same random deployment.

Alchemist simulations can be reproduced by feeding them the same random number generator.
This assumption is true as far as the custom component in use:
* do not use any other random generators but the one provided by the simulation framework (all the standard components are guaranteed to do so);
* do not iterate over collections with no predicible iteration order (e.g., Java's `Set` and `Map`)
  containing elements (or keys) whose `hashCode()` has not been overridden to return the same value regardless of the specific JVM in use;
* do not run operations in parallel.

The [`seeds`](/reference/yaml/#seeds) section control the random generation process and  may contain two optional values:
[`scenario`](/reference/yaml/#scenario) and [`simulation`](/reference/yaml/#simulation).
The former is the seed of the pseudo-random generator used during the creation of the simulation, e.g. for deploying
nodes in random arrangements.
The latter is the seed of the pseudo-random generator used during the simulation, e.g. for computing time distributions
or generating random positions.
A typical example in which one may want to have different values, is to keep the same random deployment of devices in
some scenario but allow events to happen with different timings.

{{< code path="alchemist-cognitive-agents/src/test/resources/homogeneous-pedestrians.yml" >}}

Usually, in batches, you wan to run multiple runs per experiment, varying the simulation seed, in order to get more
reliable data (and appropriate error bars).
As per any other value, variables can be feeded as random generator seeds.
