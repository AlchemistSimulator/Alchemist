+++
pre = ""
title = "Simulation Engine Configuration"
weight = 5
summary = "Alchemist's reactive simulation engine."
tags = ["configuration", "engine", "reactive"]
+++

## Engine Configuration

Alchemist ships a single reactive simulation engine. It is selected by default, so normal simulations require no
engine configuration.

[Reaction Scheduling and Ownership](/explanation/metamodel/reaction-scheduling/) describes how reactions own their
observable occurrence times and how initialization, firing, invalidation, and removal differ.
The [simulation-engine explanation](/explanation/engine/) describes how the engine consumes those scheduling
decisions.

The former parallel `BatchEngine`, its fixed-size and epsilon schedulers, and output-replay strategies have been
removed. A configuration that requests `BatchEngine` fails with a compatibility error instead of silently changing
execution semantics.

Launcher parameter batches remain supported. They orchestrate independent simulations, each using the standard
reactive engine, and are distinct from processing multiple events concurrently inside one simulation.

Third-party implementations of the simulation API can still be selected through the `engine` section and the
[arbitrary class loading system](https://alchemistsimulator.github.io/reference/yaml/index.html).
