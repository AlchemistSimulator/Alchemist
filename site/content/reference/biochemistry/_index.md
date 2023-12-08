+++
title = "Biochemistry Incarnation"
weight = 5
tags = ["biochemistry", "molecule", "junction"]
summary = "Reference documentation of the reactions language for the biochemistry incarnation."
+++

## The Biochemistry DSL
Biochemistry programs are written in a and human-readable syntax.
Valid programs can be written directly into 
Those simple reactions can be fed directly as [`program`](/reference/yaml/#programprogram) in the YAML file.

### Reactions
A reaction rule can be set using the symbol ``-->`` according to chemistry equations,
and placing both the molecules and the actions inside two square brackets
(ex. ``[OH]``, ``[H2O]``, ``[BrownianMove(0.1)]``)

The following line represents a basic chemical reaction that happens inside a cell:
``[H] + [OH] --> [H2O]``

However, reactions can also take place outside cells.
Biological cells, indeed, can swap molecules with its neighbour or the surrounding environment,
and this is possible in Alchemist too, using the keywords:
``in cell``, ``in neighbour`` and ``in env``.

The reaction ``[A in env] --> [A in cell]`` moves the molecule A from the environement inside the cell.

If the location is not explicit, it is assumed the molecule to be inside the cell.

### Junctions

A junction can be created just with a neighbor of the programmed cell.

The way to create it is with the syntax ``[X] + [Y in neighbor] --> [junction X-Y]``,
which means that when this reaction happens a junction using the molecule ``X``
from the cell and the molecule ``Y`` from the neighbor will be created.

The junction can also be destroyed using the syntax ``[junction X-Y] --> []``,
causing the reintroduction of the molecule ``X`` inside the cell and the molecule ``Y``
inside the neighbor.

Also, the junction will be automatically removed if, because of their movement,
the cells will stop being in a neighborhood.

### Custom Conditions

Any custom condition must be placed after the reaction products following an ``if`` clause.
For example, to create a molecule if the cell has at least three neighbor you would write:

``[] --> [X] if NumberOfNeighborsGreaterThan(5)``

### Movement

A movement can be performed in the same way of a reaction, using the function as it is a product of the reaction itself.
This program constantly moves a cell without any other condition:

``[] --> [BrownianMove(0.1)]``

### Collisions

The Biochemistry Incarnation supports cell collisions and deformations too.

In order to do that, however, the environment must feature appropriate support, as for instance
{{% api package="model.biochemistry.environments" class="BioRect2DEnvironmentNoOverlap" %}}.

The cells must support deformation as well, as, for instance, a node with the
{{% api package="model.biochemistry.properties" class="CircularDeformableCell" %}}
property.

The minimum radius of the cell is so that ``min-radius = rigidity * max-radius``
and the two parameters are used to compute collisions and impacts between the cells.
