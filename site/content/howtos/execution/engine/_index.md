+++
pre = ""
title = "Simulation Engine Configuration"
weight = 5
summary = "Alchemist's reactive simulation engine."
tags = ["configuration", "engine", "reactive"]
+++

## Engine Configuration

Alchemist ships a reactive simulation {{% api package="core" class="Engine" %}}.
It is selected by default, so normal simulations require no engine configuration.

[Reaction Scheduling and Ownership](/explanation/metamodel/reaction-scheduling/) describes how reactions own their
observable occurrence times and how initialization, firing, invalidation, and removal differ.
The [simulation-engine explanation](/explanation/engine/) describes how the engine consumes those scheduling
decisions.

Launcher parameter batches orchestrate independent simulations, each using its own reactive engine;
see [parameter sweeping with simulation batches](/howtos/execution/batch/).
Each engine executes the events of its simulation one at a time.

Third-party implementations of {{% api package="core" class="Simulation" %}} can still be selected through the
[`engine`](/reference/yaml/#engine) section and the
[arbitrary class loading system](/reference/yaml/#arbitrary-class-loading-system).

