+++
title = "Find paths indoors"
weight = 5
tags = ["node", "nodes", "pathfinding", "navigation mesh"]
summary = "How to navigate the environment, especially indoors."
+++

The background regarding the navigation system is explained [here](/explanation/pathfinding).

## Generating navigation graphs from images

If your environment is codified as an image,
generating a navigation graph is straight-forward.
All you have to do is mark the areas of the environment where to plant initial seeds in blue (RGB #0000FF).
In the image below you can see the generation of a navigation graph.
The blue regions in the original image indicate where to plant initial seeds.
These are then grown and crossings are found between them.

![navigation graph generation](/explanation/pathfinding/navigation-graph-generation.jpeg)

Once you have your image ready for the generation of the navigation graph, you can exploit the
{{% api package="model.cognitive.environments" class="ImageEnvironmentWithGraph" %}} class to produce it for you.
This will read your image, extract the positions you marked blue and pass them to the NaviGator algorithm.

### Examples

{{< code path="alchemist-cognitive-agents/src/test/resources/complete-knowledge.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/congestion-avoidance.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/explore.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/follow-route.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/multiple-orienting-pedestrians.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/nearest-door.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/no-knowledge.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/partial-knowledge.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/pursue.yml" >}}
{{< code path="alchemist-cognitive-agents/src/test/resources/reach-destination.yml" >}}
{{< code path="src/test/resources/website-snippets/cognitive-orienting-pedestrian.yml" >}}
{{< code path="src/test/resources/website-snippets/homogeneous-orienting-pedestrian.yml" >}}
{{< code path="src/test/resources/website-snippets/physical-steering-strategies.yml" >}}
