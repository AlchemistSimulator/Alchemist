+++
title = "Biochemistry Incarnation"
weight = 5
tags = ["biochemistry", "molecule", "reaction", "junction"]
summary = "Basics of the biochemistry incarnation."
+++

Biochemistry is an incarnation of Alchemist developed to provide support for biochemical reactions
that take place inside a biological cell or a group of those surrounded by a common environment.

### The Biochemistry Incarnation

The Biochemistry incarnation provides ways to:

* Manage the creation, destruction and relocation of a molecule (which can be either a simple atom or a complex protein)
  inside a cell or from a cell to another
* Create junctions between cells using a specified amount of molecules.
  The junctions are modeled in a general way,
  but with a simple use of actions and conditions it will be possible to create tight junctions,
  anchoring junctions, gap junctions and even custom one
* Move a cell inside its environment in different ways,
  handling collisions between two ore more of them in a simple but effective way

## The Biochemistry DSL
Biochemistry programs are encapsulated inside the YAML configuration file with a simple and human-readable syntax.
Those simple reactions can be written in the section ``programs`` of the configuration file, as value of the ``program`` key:
```yaml
programs:
  -
    - time-distribution: 1
      program: "[ATP] --> [ADP] + [P]"
```

### Reactions
A reaction rule can be set using the symbol ``-->`` according to chemistry equations, and placing both the molecules and the actions inside two square brackets (ex. ``[OH]``, ``[H2O]``, ``[BrownianMove(0.1)]``)

The following line, so, represents a basic chemical reaction that happens inside a cell: ``[H] + [OH] --> [H2O]``

However, reactions can also take place outside the cell itself. Biological cells, indeed, can swap molecules with its neighbour or the surrounding environment, and this is possible in Alchemist too, using the keywords: ``in cell``, ``in neighbour`` and ``in env``.

The reaction ``[A in env] --> [A in cell]`` moves the molecule A from the environement inside the cell.

If the location is not explicit, it is assumed the molecule to be inside the cell.

### Scheduling

{{% api package="model.biochemistry.reactions" class="BiochemicalNodeReaction" %}} computes its rate from the
configured exponential rate and the typed state of its conditions.
Molecule quantities determine mass-action factors, neighbor conditions provide selection weights, and mechanical
conditions contribute tension-dependent factors.
Changes to these inputs refresh scheduling even when condition validity remains true.
It is a Markovian reaction: the general rules for preserving and rescaling its pending occurrence are described in
[Reaction Scheduling and Ownership](/explanation/metamodel/reaction-scheduling/#reactive-invalidation).

The binomial mass-action calculation requires discrete molecule counts: local, neighboring, and extracellular
quantities must be non-negative integers no greater than `Int.MAX_VALUE`.
Fractional concentrations require a reaction type with a continuous rate law.
Each factor is validated even when another factor makes the total rate zero.
Zero propensity suspends scheduling, and positive infinite propensity schedules an immediate occurrence subject to
the distribution's start time.
Negative and NaN propensities fail at the reaction's scheduling boundary.

### Junctions
A junction can be created just with a neighbor of the programmed cell.

The way to create it is with the syntax ``[X] + [Y in neighbor] --> [junction X-Y]``, which means that when this reaction happens a junction using the molecule ``X`` from the cell and the molecule ``Y`` from the neighbor will be created.

The junction can also be destroyed using the syntax ``[junction X-Y] --> []``, causing the reintroduction of the molecule ``X`` inside the cell and the molecule ``Y`` inside the neighbor.

Also, the junction will be automatically removed if, because of their movement, the cells will stop being in a neighborhood.

### Custom Conditions
A custom condition is placed after the reaction products following an `if` clause.
{{% api package="model.biochemistry.reactions" class="BiochemicalNodeReaction" %}} accepts quantity conditions
derived from {{% api package="model.biochemistry.conditions" class="GenericMoleculePresent" %}}, neighbor conditions
derived from {{% api package="model.biochemistry.conditions" class="AbstractNeighborCondition" %}},
{{% api package="model.biochemistry.conditions" class="EnvPresent" %}}, and
{{% api package="model.biochemistry.conditions" class="TensionPresent" %}}.
These types expose the state used by the reaction's rate law; their validity controls whether the reaction can run.

### Movement
A movement can be performed in the same way of a reaction, using the function as it is a product of the reaction itself.

This program constantly moves a cell without any other condition:

``[] --> [BrownianMove(0.1)]``

### Collisions
The Biochemistry Incarnation supports cell collisions and deformations too.

In order to do that, however, you must set this environment:
```yaml
environment:
  type: BioRect2DEnvironmentNoOverlap
```

Then, when creating the cells, you must use these specific implementations:
```yaml
nodes:
  type: CircularDeformableCellImpl
  parameters: [max-radius, rigidity]
```

The minimum radius of the cell is so that ``min-radius = rigidity * max-radius`` and the two parameters are used to compute collisions and impacts between the cells.
