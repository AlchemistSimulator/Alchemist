+++
title = "Simulate Physical Interactions"
weight = 5
tags = ["physics", "interaction", "pedestrian"]
summary = "Physical interaction between nodes"
+++

Before reading the following explanation you may want to look at the
[explanation of the cognitive agents](/explanation/cognitive) and then at 
[how to work with them](/howtos/simulation/cognitive).

## Configuring the physics environment

The simulator is equipped with a special type of {{%api class=Environment %}} 
named {{%api package=model.implementations.environments class=EnvironmentWithDynamics %}}
which performs collision detection and response between nodes and with obstacles. 

Here's a minimal configuration of a physics simulation:
{{< code path="src/test/resources/website-snippets/minimal-physics-configuration.yml" >}}

It's also possibile to specify an image path for including obstacles in the environment
{{< code path="src/test/resources/website-snippets/physics-with-obstacles.yml" >}}

## Adding nodes to the environment

Nodes added to the {{%api package=model.implementations.environments class=EnvironmentWithDynamics %}} 
are required to have at least a {{%api package=model.interfaces.properties class=PedestrianProperty %}},
a {{%api package=model.interfaces.properties class=PhysicalPedestrian %}} and a 
{{%api package=model.interfaces.properties class=OccupiesSpaceProperty %}}.

Here's an example:
{{< code path="src/test/resources/website-snippets/minimal-physical-pedestrian.yml" >}}

## Configuring nodes programs

When using the `EnvironmentWithDynamics`, any suitable {{%api class=Reaction %}} can be used,
however, in order to take advantage of the physical micro-interactions between nodes such as avoidance, 
pushing behavior and falls, derived from the work of [Pelechano et al.](https://bit.ly/3e3C7Tb) you need 
to use the {{%api package=model.implementations.reactions class=PhysicalBlendedSteering %}}.

Here's an example:
{{< code path="src/test/resources/website-snippets/physical-steering-strategies.yml" >}}

## Updating the physics engine

The `EnvironmentWithDynamics` internally uses a {{%api class=GlobalReaction %}} called 
{{%api package=model.implementations.reactions class=PhysicsUpdate %}} to update nodes positions. 
By deault this reaction uses a {{%api package=model.implementations.timedistributions class=DiracComb %}}
with a default rate. If you want, it's possible to override the reaction with a custom 
{{%api class=TimeDistribution %}} and update rate.

Here's some examples:
{{< code path="src/test/resources/website-snippets/customize-physics-update-rate1.yml" >}}

{{< code path="src/test/resources/website-snippets/customize-physics-update-rate2.yml" >}}

{{< code path="src/test/resources/website-snippets/customize-physics-update-rate3.yml" >}}




