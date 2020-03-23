---

title: "Using Cognitive Pedestrians"

---

Alchemist is capable of simulating the movement of pedestrians with sophisticated cognitive capabilities:

![demo]({{ 'assets/media/usage/no-knowledge.gif'|asset }})

The animation above shows an adult male with no previous knowledge of the environment trying to reach the destination marked green.

### Prerequisites
This guide assumes you already know [the Alchemist metamodel](../simulator/metamodel.md), [how to write simulations in YAML](yaml.md) and [how to generate a navigation graph](navmeshes.md).

### Types of pedestrians
The characteristics of the pedestrians loadable in a simulation can be chosen from three available types, 
each representing a more refined version of the previous one.

#### Homogeneous Pedestrian
Homogeneous pedestrians are a particular type of _Node_ which have no peculiar characteristic each other. 
It is the most common type of pedestrian which can be load in a simulation.

```yaml
displacements:
  - in:
      type: Circle
      parameters: [100, 0, 0, 20]
    nodes:
      type: HomogeneousPedestrian2D
```

#### Heterogeneous Pedestrian
Heterogeneous pedestrians are _Nodes_ who have a given age and gender, based on which their speed, compliance 
and social attitudes are computed.
The kinds of age which can be used are: *child*, *adult*, *elderly*; alternatively you can specify the exact age as a number.
The kinds of sex available are: *male*, *female*.

```yaml
displacements:
  - in:
      type: Circle
      parameters: [50, 0, 0, 20]
    nodes:
      type: HeterogeneousPedestrian2D
      parameters: ["elderly", "female"]
  - in:
      type: Circle
      parameters: [50, 0, 0, 20]
    nodes:
      type: HeterogeneousPedestrian2D
      parameters: ["child", "male"]
```

#### Cognitive Pedestrian
A cognitive pedestrian is a heterogeneous pedestrian with cognitive capabilities. It has an emotional state and is able to influence and to be influenced by other people with the same capabilities. For instance, cognitive pedestrians can perceive fear, not just directly but also by contagion (e.g. seeing other people fleeing may cause them flee as well).

```yaml
reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [1.0]
    type: CognitiveBehavior

displacements:
  - in:
      type: Circle
      parameters: [50, 0, 0, 20]
    nodes:
      type: CognitivePedestrian2D
      parameters: ["adult", "male"]
    programs:
      - *behavior
  - in:
      type: Circle
      parameters: [50, 0, 0, 20]
    nodes:
      type: CognitivePedestrian2D
      parameters: ["adult", "female"]
    programs:
      - *behavior
```

### Groups
It is likely that a pedestrian doesn't move on its own, but there is a group consisting of multiple people 
which are related each other and whose behaviors are strictly dependant on that structure.
The only way you can currently assign a group to a pedestrian is by creating it as a variable and passing it 
as a parameter when the _Nodes_ created are of pedestrian type. If you don't specify any group in this phase, 
automatically a new group of type Alone is assigned.

The following simulation example loads two groups of homogeneous pedestrians representing friends around the center of the scene, one having 10 members and the other 15.

```yaml
incarnation: protelis

variables:
  group1: &group1
    formula: it.unibo.alchemist.model.implementations.groups.Friends<Any>()
    language: kotlin
  group2: &group2
    formula: it.unibo.alchemist.model.implementations.groups.Friends<Any>()
    language: kotlin

displacements:
  - in:
      type: Circle
      parameters: [10, 0, 0, 20]
    nodes:
      type: HomogeneousPedestrian2D
      parameters: [*group1]
  - in:
      type: Circle
      parameters: [15, 0, 0, 20]
    nodes:
      type: HomogeneousPedestrian2D
      parameters: [*group2]
```

### Steering Actions
A pedestrian that doesn't move is not a pedestrian. The need of _Actions_ which can make it move in a realistic way
inside the environment lead to the creation of particular behaviors such as _Flee_, _Wander_, _FollowFlowField_, _ObstacleAvoidance_... <br />
As a potential part of a group, there must exist also some attitudes typical of this sort of formation such as _Cohesion_ and _Separation_.
The creation of complex movements can be accomplished by combining different steering actions together. <br />
The only way currently available to do so, it is to use some _SteeringBehavior_ extending _Reaction_, which can recognize, across all the actions specified, the steering ones
to trait them in a separate way.

In this simulation 50 people wander around the environment and if they are approaching an obstacle they avoid it.

```yaml
incarnation: protelis

environment:
  type: ImageEnvironment
  parameters: [...]

reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [3.0]
    type: PrioritySteering
    actions:
      - type: RandomRotate
      - type: Wander
        parameters: [6, 4]
      - type: ObstacleAvoidance
        parameters: [4]

displacements:
  - in:
      type: Circle
      parameters: [50, 0, 0, 25]
    nodes:
      type: HomogeneousPedestrian2D
```

### Steering Strategies
In order to decide the logic according to which the different steering actions must be combined, 
the concept of steering strategy has been introduced and related to it different reactions are available to be used
with the aim of computing the desired route for the pedestrians.
If you want a pedestrian to execute a single steering action at a time, _PrioritySteering_ is a reaction 
which gives relevance only to the steering action whose target point is the nearest to the current pedestrian position.
If you want a pedestrian to execute a movement considering multiple actions at a time, _BlendedSteering_ weights them
considering their target distance to the current pedestrian position.
There is no limit to the number of steering actions which can be used together but some messy compositions 
can result in unpredictable behaviors, so pay attention.

In the example below a pedestrian reaches a point of interest, avoiding in the meantime to approach another position.

```yaml
incarnation: protelis

environment:
  type: Continuous2DEnvironment

reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [1.0]
    type: BlendedSteering
    actions:
      - type: Seek
        parameters: [1000, 500]
      - type: Flee
        parameters: [500, -500]

displacements:
  - in:
      type: Point
      parameters: [0, 0]
    nodes:
      type: HomogeneousPedestrian2D
    programs:
      - *behavior
```

### Orienting abilities

As showed in the animation on the top of the page, pedestrians can be equipped with orienting abilities and different knowledge degrees of the environment. The latter quantity indicates the level of knowledge of the pedestrian concerning the environment prior to the start of the simulation, thus it does not take into account the knowledge the pedestrian will gain during it. Of the above described types of pedestrians only homogeneous and cognitive pedestrians can be equipped with orienting abilities.

#### Homogeneous orienting pedestrians

The only parameters you need to provide when instancing a homogeneous orienting pedestrian is its knowledge degree and a navigation graph of the environment:
```yaml
variables:
  envGraph: &envGraph
    formula: |
      import it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon.Deaccon2D
      import java.awt.geom.Point2D
      import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
      Deaccon2D().generateEnvGraph(
        Point2D.Double(0.0, 0.0), 
        100.0, 
        100.0, 
        ImageEnvironment<Number>("path/to/myEnv.png").obstacles, 
        mutableListOf(Euclidean2DPosition(50.0, 50.0))
      )
    language: kotlin

displacements:
  - in:
      type: Point
      parameters: [0, 0]
    nodes:
      type: OrientingHomogeneousPedestrian2D
      parameters: [0.5, *envGraph]
```
The knowledge degree is a `Double` value in [0, 1] describing the percentage of the environment the pedestrian is familiar with. The navigation graph of the environment is a complex data structure which can be obtained using the `Deaccon2D` class, and can be instanced in the simulation file using the `variables` key. For more information refer to [how to generate a navigation graph](navmeshes.md) and [how to write simulations in YAML](yaml.md). Note that despite their name, the knowledge degree of different homogeneous orienting pedestrians may differ, and even pedestrians with the same knowledge degree can be different as each one can be familiar with different portions of the environment.

#### Cognitive orienting pedestrian

Cognitive orienting pedestrians can be instanced providing knowledge degree and navigation graph of the environment before all the other parameters needed. Remember to provide them with cognitive behavior or their emotions won't evolve at all:
```yaml
variables:
  envGraph: &envGraph
    formula: |
      import it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon.Deaccon2D
      import java.awt.geom.Point2D
      import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
      Deaccon2D().generateEnvGraph(
        Point2D.Double(0.0, 0.0), 
        100.0, 
        100.0, 
        ImageEnvironment<Number>("path/to/myEnv.png").obstacles, 
        mutableListOf(Euclidean2DPosition(50.0, 50.0))
      )
    language: kotlin

reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [1.0]
    type: CognitiveBehavior

displacements:
  - in:
      type: Point
      parameters: [0, 0]
    nodes:
      type: OrientingCognitivePedestrian2D
      parameters: [0.5, *envGraph, "adult", "male"]
    programs:
      - *behavior
```

#### Orienting behavior

Similarly to cognitive pedestrians and the correspondent behavior, in order for orienting pedestrians to be able to navigate the environment, you need to provide them with the orienting behavior. Such object is a _reaction_ that will exploit the spatial information available to a pedestrian in order to navigate the environment towards (or in search of) a destination. It will also register new information gained by the pedestrian during the simulation.

The only parameter you need to specify when declaring an orienting behavior is the navigation graph of the environment:
```yaml
variables:
  envGraph: &envGraph
    formula: |
      import it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon.Deaccon2D
      import java.awt.geom.Point2D
      import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
      Deaccon2D().generateEnvGraph(
        Point2D.Double(0.0, 0.0), 
        100.0, 
        100.0, 
        ImageEnvironment<Number>("path/to/myEnv.png").obstacles, 
        mutableListOf(Euclidean2DPosition(50.0, 50.0))
      )
    language: kotlin

reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [1.0]
    type: OrientingBehavior2D
    parameters: [*envGraph]
```
As you may have noted, no destination is specified. This is because the navigation graph provides a set of destinations itself, which will be used by the orienting behavior.

### Evacuation Scenarios
Pedestrians can be loaded in any kind of _Environment_ but it is recommended to use _PhysicsEnvironments_ since they
have properties such as non-overlapping shapes which are advisable to be taken into consideration 
when working with a crowd.
To specify the existence of a potential danger or a significative zone in general inside the environment you can use _Layers_. 
Each layer is associated to a _Molecule_ different from the one of all the others.
You must specify to any cognitive pedestrian the _Molecule_ representing danger in the _Environment_, 
otherwise it won't have the ability to recognize the presence of it.

In the following example 100 adult females with cognitive capabilities get away from a zone in the environment where
there is a potential danger.

```yaml
incarnation: protelis

variables:
  danger: &danger
    formula: "\"danger\""

environment:
  type: Continuous2DEnvironment

layers:
  - type: BidimensionalGaussianLayer
    molecule: *danger
    parameters: [0.0, 0.0, 20.0, 15.0]

reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [1.0]
    type: PrioritySteering
    actions:
      - type: AvoidFlowField
        parameters: [*danger]

displacements:
  - in:
      type: Circle
      parameters: [100, 0, 0, 50]
    nodes:
      type: CognitivePedestrian2D
      parameters: ["adult", "female", *danger]
    programs:
      - *behavior
```

### Further references
[C. Natalie van der Wal, Daniel Formolo, Mark A. Robinson, Michael Minkov, Tibor Bosse\
Simulating Crowd Evacuation with Socio-Cultural, Cognitive, and Emotional Elements\
Transactions on Computational Collective Intelligence XXVII. 2017.](https://doi.org/10.1007/978-3-319-70647-4_11)

[Craig W. Reynolds\
Steering Behaviors for Autonomous Characters. 1999.](http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.16.8035)

[Erik Andresen, Mohcine Chraibi & Armin Seyfried\
A representation of partial spatial knowledge: a cognitive map approach for evacuation simulations](https://www.tandfonline.com/doi/full/10.1080/23249935.2018.1432717)