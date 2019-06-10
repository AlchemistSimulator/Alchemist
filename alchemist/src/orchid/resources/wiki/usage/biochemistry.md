---

title: Alchemist biochemistry incarnation

---


Biochemistry is an incarnation of Alchemist developed to provide support for biochemical reactions that take place inside a biological cell or a group of those surrounded by a common environment.

### The Biochemistry Incarnation

The Biochemistry incarnation provides a way to:

* Manage the creation, destruction and relocation of a molecule (which can be either a simple atom or a complex protein) inside a cell or from a cell to another
* Create junctions between cells using a specified amount of molecules. The junctions are modeled in a general way, but with a simple use of actions and conditions it will be possible to create tight junctions, anchoring junctions, gap junctions and even customized one
* Move a cell inside its environment in different ways, handling collisions between two ore more of them in a simplistic but effective way

### How To Run A Simulation
As first step, it is required to add this line of code in order to notice Alchemist to use the Biochemistry Incarnation:

```yaml
incarnation: biochemistry
```

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

### Junctions
A junction can be created just with a neighbor of the programmed cell.

The way to create it is with the syntax ``[X] + [Y in neighbor] --> [junction X-Y]``, which means that when this reaction happens a junction using the molecule ``X`` from the cell and the molecule ``Y`` from the neighbor will be created.

The junction can also be destroyed using the syntax ``[junction X-Y] --> []``, causing the reintroduction of the molecule ``X`` inside the cell and the molecule ``Y`` inside the neighbor.

Also, the junction will be automatically removed if, because of their movement, the cells will stop being in a neighborhood.

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
