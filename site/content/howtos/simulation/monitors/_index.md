+++
title = "Monitoring Simulations through Custom Output Monitors"
weight = 5
tags = ["monitors", "simulation monitoring", "output monitors"]
summary = "Create custom monitors to track simulation progression and interact with standard hooks."
+++

In Alchemist, custom monitors provide a flexible way to observe the progression of simulations and respond to standard hooks. To set up a custom monitor, follow the steps below:

1. **Extend the OutputMonitor Class:**
   Create a new class extending the {{%api package="boundary" class="OutputMonitor" %}} class.
    ```kotlin
    package it.unibo.foo
    import it.unibo.alchemist.model.boundary.OutputMonitor
    import it.unibo.alchemist.model.interfaces.Position

    class FooMonitor<T, P : Position<P>> : OutputMonitor<T, P>()
    ```

2. **Update Simulation Configuration:**
   Add your custom monitor to the simulation configuration file.
    ```yaml
    incarnation: protelis

    monitors:
      - type: it.unibo.foo.FooMonitor
    ```
