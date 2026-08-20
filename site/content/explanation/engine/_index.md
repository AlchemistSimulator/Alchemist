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
The current engine is reactive: it does not build or maintain a dependency graph.
The original dependency-graph design is described in
[this scientific paper in the Journal of Simulation](https://dx.doi.org/10.1057/jos.2012.27),
but it is not the current scheduling architecture.

The engine's entry point is the `Simulation`.
It is equipped with support for commands like play, pause and stop, and can be equipped with an `OutputMonitor`.
The output monitor can be a graphical interface, a logger or any kind of environment inspector.

Reaction occurrence ownership, condition-driven invalidation, and the initialization, firing, and removal
transitions are model semantics. They are described in
[Reaction Scheduling and Ownership](/explanation/metamodel/reaction-scheduling/).

## Engine responsibilities

The engine consumes the model's scheduling decisions without computing them or inferring model dependencies.
It owns the simulation clock, command processing, output-monitor notifications, runtime reaction membership, and
the scheduler that selects the earliest occurrence.

For each active reaction, the engine owns one exact subscription to the reaction's observable occurrence.
An emission causes the scheduler to reindex that reaction immediately.
Reaction registration, occurrence updates, scheduler operations, and removal are confined to the simulation thread;
external mutations must enter through the simulation command queue.

Nodes and reactions may be added or removed while a simulation is running.
Registration initializes the reaction before scheduler insertion, while removal releases the engine-owned
subscription before removing the scheduler entry and disposing the reaction.
