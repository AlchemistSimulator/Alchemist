+++
title = "SAPERE Incarnation"
weight = 5
tags = ["sapere", "lsa", "live semantic annotation", "tuple space", "tuple centre"]
summary = "Basics of SAPERE and how its concepts are mapped in Alchemist."
+++

The SAPERE incarnation for Alchemist was the first stable incarnation produced for the simulator.
It was developed in the context of the [SAPERE EU project](http://archive.is/umlcC).

At the core of [SAPERE](https://doi.org/10.1016/j.pmcj.2014.12.002) is the concept of *Live Semantic Annotation* (LSA),
namely a description of a resource (sensor, service, actuator...) always mapping the current resource status
(somewhat a prelude to the currently famous [digital twin](http://archive.is/YR1v9) concept).

These annotations evolve following so-called *Eco-Laws*,
mimicking the complex behaviours of natural ecosystems.

The SAPERE approach fostered subsequent approaches, such as [aggregate computing](https://doi.org/10.1109/MC.2015.261).

## Live Semantic Annotations

An LSA as modeled in Alchemist is a tuple of values.
These tuples can be injected in nodes as data items.
From the point of view of
[the Alchemist metamodel](/explanation/metamodel),
the concept of
{{% api class="Molecule" %}}
is mapped to 
**LSA** ({{% api package="model.sapere.molecules" class="LsaMolecule" %}}).
As a consequence, LSAs can be inserted in nodes.

## Eco-Laws

Tuple matching is used to define Eco-Laws.
An Eco-Law is a rewriting rule very similar in concept to chemical reactions:
elements on the left-hand side of the reaction are removed from the container,
elements on the right-hand side are inserted instead.

The following program matches LSAs with two arguments, the former must be `foo`,
the latter a number greater than `30`, and produces in a new tuple having as first element `bar` and as second
the opposite of the matched number:

`{ foo, def: N > 30 } --> { bar, -N }`