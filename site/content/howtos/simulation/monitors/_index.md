+++
title = "Observe Simulations with Output Monitor"
weight = 5
tags = ["monitors", "monitor", ]
summary = "Define ad-hoc monitors to observe the simulation evolution and react to standard hooks"
+++

In Alchemist it is possible to define ad-hoc monitors to observe the simulation evolution and react to standard hooks.
In order to do so, you need to create a class that extends the {{%api package=model.boundary class=OutputMonitor %}} class 
and add it to the simulation configuration file.

Firstly, you need to create your own class that extends the {{%api package=model.boundary class=OutputMonitor %}} class:
```
package it.unibo.foo
import it.unibo.alchemist.model.boundary.OutputMonitor
import it.unibo.alchemist.model.interfaces.Position

class FooMonitor<T, P : Position<P>> : OutputMonitor() 
```

Then, you need to add the monitor to the simulation configuration file:
```
incarnation: protelis

monitors:
  - type: it.unibo.foo.FooMonitor
```