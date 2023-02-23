+++
title = "Pathfinding"
weight = 5
tags = ["node", "nodes", "pathfinding", "navigation mesh"]
summary = "Strategies to navigate the environment."
+++

This section explains the pathfinding strategies and algorithms of Alchemist.
Instructions on how to exercise them are available [here](/howtos/simulation/environment/pathfinding).

## Navigation graphs

A *navigation graph* of an environment with obstacles is a graph whose nodes are convex shapes representing portions of
the environment which are traversable by agents (namely, walkable areas), and edges represent connections between them.
The image below shows a bidimensional environment with obstacles on the left and the associated navigation graph on the
right (nodes are painted blue,
edges are represented as line segments connecting the centroid of a node to the associated crossing,
which is painted green).

![navigation graph](navigation-graph.jpeg)

Navigation graphs are mainly used for navigation purposes (e.g. in pathfinding algorithms):
the advantage of decomposing the environment into convex regions is that agents can freely walk around within a convex region,
as it is guaranteed that no obstacle will be found
(remember that a shape is convex when no line segment between any two points on its boundary ever goes outside the shape).

Alchemist is capable of generating navigation graphs of bidimensional environments featuring euclidean geometry
and double precision coordinates.
Before diving into the topic,
please be aware that the algorithm implemented in Alchemist for the generation of navigation graphs:
- Does not guarantee the coverage of 100% of the walkable area (as the image above shows).
- Is only capable to detect axis-aligned crossings.
- Is only capable to deal with convex polygonal obstacles. Concave ones can be decomposed into convex meshes,
  whereas for curves bounding boxes can be used, eventually arbitrarily-oriented minimum bounding boxes.

### NaviGator

The algorithm implemented in Alchemist is called NaviGator (Navigation Graphs generAtor),
here's a brief description of how it operates: firstly, a certain number of seeds is planted in the environment.
Each seed is a square-shaped region of unitary side that will grow maintaining a convex shape.
Secondly, planted seeds are extended as far as possible
(i.e. until they are in contact with an obstacle or another seed on each side).
Finally, crossings are found between the grown seeds.
NaviGator is derived from the
[DEACCON algorithm](https://doi.org/10.1609/aiide.v4i1.18693)
for the generation of navigation meshes.
