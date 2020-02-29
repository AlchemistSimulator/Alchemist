---

title: "Using Cognitive Agents"

---

Alchemist is capable of simulating pedestrians with orienting capabilities:

![demo]({{ 'assets/media/usage/no-knowledge.gif'|asset }})

In particular, pedestrians with different degrees of knowledge of the environment can be simulated. The animation above shows a pedestrian with no previous knowledge of the environment trying to reach the destination marked green.

### Prerequisites
This guide assumes you already know [the Alchemist metamodel](../simulator/metamodel.md), [how to write simulations in YAML](yaml.md), [how to generate a navigation graph](navmeshes.md) and [how to use cognitive agents](cognitive-agents.md).

### Orienting pedestrians

Orienting pedestrians are capable of navigating inside an environment, and posses a certain knowledge degree of it. Such quantity indicates the level of knowledge of the pedestrian concerning the environment prior to the start of the simulation, thus it does not take into account the knowledge the pedestrian will gain during it. There are two types of orienting pedestrians.

#### Homogeneous orienting pedestrians

Homogeneous orienting pedestrians are a particular type of _Node_ which have no peculiar characteristic each other, a part from the knowledge degree which of course may differ. Note that even pedestrians with the same knowledge degree can be different as each one can be familiar with different portions of the environment. Assuming you already specified the environment and the random seeds to use (for more information see [how to write simulations in YAML](yaml.md)), the only parameters you need to provide to instance a homogeneous orienting pedestrian is its knowledge degree and a navigation graph of the environment:
```yaml
displacements:
  - in:
      type: Point
      parameters: [0, 0]
    nodes:
      type: OrientingHomogeneousPedestrian2D
      parameters: [0.5, *envGraph]
```
The knowledge degree is a `Double` value in [0, 1] describing the percentage of the environment the pedestrian is familiar with. The navigation graph of the environment is a complex data structure which is assumed to be instanced somewhere else in the simulation file. You can obtain such a data structure using the `Deaccon2D` class, and you can instance it in the simulation file using the `variables` key. For more information refer to [how to generate a navigation graph](navmeshes.md) and [how to write simulations in YAML](yaml.md). 

There is a third parameter you may want to specify when instancing a homogeneous orienting pedestrian: the group of people he/she belongs to (every pedestrian is alone by default). Such group can influence the pedestrian's movements, which e.g. will try to remain close to his/her group's members. For more information see [how to use cognitive agents](cognitive-agents.md).

#### Cognitive orienting pedestrian

Cognitive orienting pedestrian are cognitive pedestrians (i.e. they have heterogeneous characteristics as well as cognitive capabilities, such as emotions and social contagion) with orienting abilities too. You can instance them providing knowledge degree and navigation graph of the environment before all the other parameters needed for cognitive pedestrians (refer to [how to use cognitive agents](cognitive-agents.md)). Remember to provide them with cognitive behavior or their emotions won't evolve at all:
```yaml
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

Similarly to cognitive pedestrians and the correspondent behavior, in order for an orienting pedestrian to be able to navigate the environment you need to provide him/her with the orienting behavior. Such object is a _reaction_ that will exploit the spatial information available to the pedestrian in order to navigate the environment towards (or in search of) a destination. It will also register new information gained by the pedestrian during the simulation.

The only parameter you need to specify when declaring an orienting behavior is the navigation graph of the environment:
```yaml
reactions: &behavior
  - time-distribution:
      type: DiracComb
      parameters: [1.0]
    type: OrientingBehavior2D
    parameters: [*envGraph]
```
As you may have noted, no destination is specified. This is because the navigation graph provides a set of final destinations itself, which will be used by the orienting behavior.

### Further references

[Erik Andresen, Mohcine Chraibi & Armin Seyfried\
A representation of partial spatial knowledge: a cognitive map approach for evacuation simulations](https://www.tandfonline.com/doi/full/10.1080/23249935.2018.1432717)
