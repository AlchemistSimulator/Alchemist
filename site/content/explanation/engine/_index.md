+++
pre = ""
title = "The Alchemist Simulation Engine"
weight = 2
tags = ["engine", "gibson-bruck", "gillespie", "discrete-event simulation", "time", "optimization"]
summary = "How does Alchemist simulate? What is at its core?"
+++

The core part of the tool is the incarnation-agnostic simulation engine.
Its current implementation is based on [Gibson and Bruck's Next Reaction](https://pubs.acs.org/doi/10.1021/jp993732q),
extended to support adding and removing reactions and nodes while a simulation is running.

The engine's entry point is {{% api package="core" class="Simulation" %}}.
It provides commands such as play, pause, and stop and publishes simulation progress to
{{% api package="boundary" class="OutputMonitor" %}} implementations.
The output monitor can be a graphical interface, a logger or any kind of environment inspector.

Reaction occurrence ownership, condition-driven invalidation, and the initialization, firing, and removal
transitions are model semantics. They are described in
[Reaction Scheduling and Ownership](/explanation/metamodel/reaction-scheduling/).

## Engine responsibilities

Each {{% api package="model" class="Reaction" %}} publishes its absolute `nextOccurrence`.
The engine owns the simulation clock, command processing, output-monitor notifications, runtime scheduling
subscriptions, and the scheduler that selects the earliest occurrence.

For each active reaction, the engine owns one exact subscription to `nextOccurrence`.
Each emitted occurrence causes the scheduler to reindex that reaction immediately.
Reaction registration, occurrence updates, scheduler operations, and removal are confined to the simulation thread;
external mutations must enter through the simulation command queue.

Nodes and reactions may be added or removed while a simulation is running.
Registration initializes the reaction before scheduler insertion, while removal releases the engine-owned
subscription before removing the scheduler entry and disposing the reaction.
