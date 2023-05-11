+++
title = "Cognitive Agents"
weight = 5
tags = ["cognitive", "pedestrian", "agents"]
summary = "Agents with realistic human behavior."
+++

We recommend to read our [explanation of the cognitive agents](/explanation/cognitive)
to better understand the contents of this how-to.

Different kinds of pedestrians are obtainable by attaching
{{% api class=NodeProperty %}}s
to nodes (e.g {{%api package=model.nodes class=GenericNode %}}).
Common properties concern abilities such as perceiving other nodes
({{% api package=model.cognitive class=PerceptiveProperty %}})
and occuping space in an environment 
({{% api package=model.physics.properties class=OccupiesSpaceProperty %}}).

## Homogeneous Pedestrian

As shown in the example below, this kind of pedestrian is obtained by attaching the
{{% api package=model.cognitive.properties class=Pedestrian %}}
property.

{{< code path="src/test/resources/website-snippets/homogeneous-pedestrian.yml" >}}

## Heterogeneous Pedestrian

The age groups available are: *child*, *adult*, and *elderly*;
alternatively, if the exact age is specified,
they are assigned to one of the aforementioned groups automatically.
The genders available are: *male* and *female*. 
This informations is included in the 
{{%api package=model.cognitive.properties class=Human %}}
property and it is used by the 
{{%api package=model.cognitive.properties class=HeterogeneousPedestrian %}} 
property, along with the age.

{{< code path="src/test/resources/website-snippets/heterogeneous-pedestrian.yml" >}}

## Cognitive Pedestrian
Cognitive pedestrians are heterogeneous pedestrians with cognitive capabilities given by a
{{% api package=model.cognitive class=CognitiveProperty %}}.
They have an emotional state and are able to influence and be influenced by others with the same capabilities.
As an example, cognitive pedestrians can perceive fear via social contagion 
(e.g. seeing other people fleeing may cause them flee as well despite they haven't directly seen the danger).
To express how a cognitive pedestrians move, based on their emotional state, attach the
{{% api package=model.cognitive.properties class=CognitivePedestrian %}}
property.

{{< code path="src/test/resources/website-snippets/cognitive-pedestrian.yml" >}}

## Homogeneous orienting pedestrian

These are homogeneous pedestrians that can be equipped with a given knowledge degree of the environment.
Such quantity is a `Double` value in `[0,1]`
describing the percentage of environment the pedestrian is familiar with prior to the start of the simulation
(thus it does not take into account the knowledge the pedestrian will gain during it).
Note that despite their name ("homogeneous"),
knowledge degrees of different homogeneous orienting pedestrians may differ,
and even pedestrians with the same knowledge degree can be different as each one
can be familiar with different portions of the environment.
Be also aware that orienting pedestrians can only be placed in an
{{% api package="model.environments" class="EnvironmentWithGraph" %}}
which is a type of environment providing a navigation graph.
In order to give a node orienting capabilities enhance a node with an
{{% api package=model.cognitive class=OrientingProperty %}}.

{{< code path="src/test/resources/website-snippets/homogeneous-orienting-pedestrian.yml" >}}

## Cognitive orienting pedestrian

These are cognitive pedestrians equipable with a given knowledge degree of the environment.

{{< code path="src/test/resources/website-snippets/cognitive-orienting-pedestrian.yml" >}}

## Groups
It is likely that a pedestrian doesn't move on its own,
but there is a group consisting of multiple people
which are related each other and whose behaviors are strictly dependent on that structure.
The only way you can currently assign a group to a pedestrian is by creating it as a variable and passing it
as a parameter when the
{{% api class="Node" %}}s
created are of pedestrian type.
If you don't specify any group in this phase,
automatically a new group of type
{{% api package="model.cognitive.groups" class="Alone" %}}
is assigned.

The following simulation example loads two groups of homogeneous pedestrians
representing friends around the center of the scene,
one having 10 members and the other 15.

{{< code path="src/test/resources/website-snippets/pedestrian-groups.yml" >}}

## Steering Actions
Steering actions are
{{% api class="Action" %}}s
whose purpose is moving a node inside an environment.
These actions  can be divided into two categories:
- greedy, i.e. performing only local choices;
- {{% api package="model.cognitive" class="NavigationAction" %}}s,
  which exploit the spatial information available to orienting pedestrians
  in order to navigate the environment consciously
  (e.g. without getting stuck in U-shaped obstacles).

For a complete overview of the available actions refer to the api documentation.
The creation of complex movements can be accomplished by combining different steering actions together.
The only way currently available to do so is by using some
{{% api package="model.cognitive.reactions" class="SteeringBehavior" %}}
extending
{{% api class="Reaction" %}}, which can recognize, across all the actions specified,
the steering ones to trait them in a separate way.

In this simulation 50 people wander around the environment and,
if they are approaching an obstacle, they avoid it.

{{< code path="src/test/resources/website-snippets/steering-actions.yml" >}}

## Steering Strategies
In order to decide the logic according to which the different steering actions must be combined, 
the concept of steering strategy has been introduced and related to it different reactions are available to be used
with the aim of computing the desired route for the pedestrians.
If you want a pedestrian to execute a single steering action at a time,
{{% api package="model.cognitive.reactions" class="PrioritySteering" %}}
is a reaction 
which gives relevance only to the steering action whose target point is the nearest to the current pedestrian position.
If you want a pedestrian to execute a movement considering multiple actions at a time,
{{% api package="model.cognitive.reactions" class="BlendedSteering" %}}
weights them considering their target distance to the current pedestrian position.
There is no limit to the number of steering actions which can be used together but some messy compositions 
can result in unpredictable behaviors, so pay attention.

In the example below a pedestrian reaches a point of interest, avoiding in the meantime to approach another position.

{{< code path="src/test/resources/website-snippets/steering-strategies.yml" >}}

## Danger and evacuations

Pedestrians can be loaded in any kind of
{{% api class="Environment" %}}
but it is recommended to use
{{% api package="model.physics.environments" class="PhysicsEnvironment" %}}s
since they
have properties such as non-overlapping shapes which are advisable to be taken into consideration
when working with a crowd.
To specify the existence of a potential danger or a significative zone in general inside the environment you can use
{{% api class="Layer" %}}s.
You must specify to any cognitive pedestrian the
{{% api class="Molecule" %}}
representing danger in the
{{% api class="Environment" %}},
otherwise it won't have the ability to recognize its presence.

In the following example,
100 adult females with cognitive capabilities get away from a zone in the environment where
there is a potential danger.

{{< code path="src/test/resources/website-snippets/evacuation-scenarios.yml" >}}

### Hardcoded parameters

Here's a list of all the hardcoded parameters.

| Name               | Value      | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|--------------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| knownImpasseWeight | 10         | Weight assigned to known impasses (= areas with a single door). It's usually a high value, allowing to avoid them.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| toleranceAngle     | 45 degrees | Used by {{% api package="model.cognitive.steering" class="SinglePrevalent" %}}, that linearly combines multiple steering actions (= multiple forces) assuming one of them is prevalent. Weights for the linear combination are determined so that the resulting force forms with the prevalent one an angle smaller than or equal to the tolerance angle. The prevalent force usually wants to move the pedestrian consciously, whereas other forces are more "greedy". The purpose of the tolerance angle is allowing to steer the pedestrian towards the target defined by the prevalent force, while using a trajectory which takes into account other urges as well. |
| alpha              | 0.5        | Used by {{% api package="model.cognitive.steering" class="SinglePrevalent" %}}, an exponential smoothing with this alpha is applied to the resulting force in order to reduce oscillatory movements.                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| maxWalkRatio       | 0.3        | Used by {{% api package="model.cognitive.steering" class="SinglePrevalent" %}}. When the pedestrian is subject to contrasting forces the resulting one may be small in magnitude, hence a lower bound for such quantity is set to (maximum distance walkable by the pedestrian) * (this parameter) so as to avoid extremely slow movements.                                                                                                                                                                                                                                                                                                                              |
| delta              | 0.05       | Used by {{% api package="model.cognitive.steering" class="SinglePrevalent" %}}. The weight assigned to disturbing forces is set to 1 and then iteratively decreased by delta until the resulting force satisfies the required conditions (see the api). This is similar to a gradient descent.                                                                                                                                                                                                                                                                                                                                                                           |

### Physical pedestrians

Physical pedestrians are capable of pushing and bumping into each other.
To express those physical interactions use a
{{% api package=model.physics.properties class=PhysicalPedestrian %}}
property.

### Physical steering strategies

In order to work properly,
physical pedestrians should be equipped with physical steering strategies.
Such strategies define how steering actions (which are intentional)
are combined with physical forces
(which are mostly unintentional).
At present, only
{{% api package="model.cognitive.reactions" class="PhysicalBlendedSteering" %}}
and
{{% api package="model.cognitive.reactions" class="NavigationPrioritizedSteeringWithPhysics" %}}
are available.

Here's a simple code for loading a homogeneous pedestrian with physical properties
with
{{% api package="model.cognitive.actions" class="CognitiveAgentSeek" %}}
and
{{% api package="model.cognitive.actions" class="CognitiveAgentFlee" %}}:

{{< code path="src/test/resources/website-snippets/physical-steering-strategies.yml" >}}
