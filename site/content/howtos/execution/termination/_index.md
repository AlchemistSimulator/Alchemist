+++
title = "Define the termination criteria"
weight = 1
tags = ["termination", "time"]
summary = "Decide when the simulator should stop and consider the simulation concluded."
+++

Alchemist supports the possibility to write termination conditions for any simulation.
Termination conditions are {{% api class="TerminationPredicate" %}}s evaluated on the environment
after every executed reaction, and, if met, cause the immediate termination of a simulation.
The default implementations are located in the {{% api package="model.terminators" %}} package.

They are defined in the [`terminate`](/reference/yaml/#terminate) section of the configuration file.
Multiple terminators are allowed: the simulation terminates as soon as any of them is satisfied
(they are combined in *or*).

## Terminating the simulation after some time

One of the simplest terminators availables allows for declaring a simulation completed when a certain simulated time is reached.
In the following example, it is used in conjunction with a number of variables, showing how it's possible to use such
variables to produce batches of simulations terminating at different times.

{{<code path="alchemist-implementationbase/src/test/resources/termination.yml" >}}

## Terminating the simulation if the environment is not changing

A terminator is provided for terminating when a simulation is "stable"
(nothing changes in terms of positions and nodes' content).
The class implementing it is {{% api package="model.terminators" class="StableForSteps" %}}.
