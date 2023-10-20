+++
pre = ""
title = "The Alchemist Simulation Engine"
weight = 2
tags = ["engine", "gibson-bruck", "gillespie", "discrete-event simulation", "time", "optimization"]
summary = "How does Alchemist simulate? What is at its core?"
+++

The core part of the tool is the incarnation-agnostic simulation engine.
Its current implementation is based on [Gibson and Bruck's Next Reaction](https://pubs.acs.org/doi/10.1021/jp993732q),
extended to support addition and removal of reactions, and improved using input and output contexts for reactions,
in order to prune the dependency graph as much as possible.
More details on that are demanded to [this scientific paper on Journal of Simulation](https://dx.doi.org/10.1057/jos.2012.27).

The engine's entry point is the `Simulation`.
It is equipped with support for commands like play, pause and stop, and can be equipped with an `OutputMonitor`.
The output monitor can be a graphical interface, a logger or any kind of environment inspector.
