+++
title = "Simulate indoor"
weight = 5
tags = ["environment", "planimetry", "indoor"]
summary = "How to create indoor environments based on planimetries."
+++

![indoor simulation](indoor_simulation.png)

Indoor environments
(bidimensional spaces with obstacles)
can be generated from images by leveraging
{{% api package="model.physics.environments" class="ImageEnvironment" %}},
which loads the map as raster image from file,
interpreting the black pixels as obstacles
(wall-like areas not accessible to nodes).
Color of pixels that represents obstacles can be set to
every color with a constructor's parameter, black is default.

By default, each pixel is considered as a 1x1 block.
As a consequence, a 1200x600 image with a vertical line of black pixels at coordinate 500 will be interpreted as a single
obstacle of size 1x600 starting at coordinate (500, 0).
It is possible to scale up or down the size of the environment by acting on the zoom parameter of
{{% api package="model.physics.environments" class="ImageEnvironment" %}},
as well as changing the initial coordinates.

## Examples

{{% projectimage "alchemist-cognitive-agents/src/test/resources/images/multiple-exits.png" %}}
{{< code path="alchemist-cognitive-agents/src/test/resources/multiple-exits.yml" >}}

---

{{% projectimage "alchemist-cognitive-agents/src/test/resources/images/obstacles.png" %}}
{{< code path="alchemist-cognitive-agents/src/test/resources/obstacle-avoidance.yml" >}}

---

{{% projectimage "src/test/resources/planimetry.png" %}}
{{< code path="src/test/resources/website-snippets/steering-actions.yml" >}}

---

{{% projectimage "src/test/resources/chiaravalle.png" %}}

* Direct reference to the image
  {{< code path="src/test/resources/website-snippets/walk-church.yml" >}}
* Search for the image in the file system via Kotlin
  {{< code path="src/test/resources/website-snippets/variables-export.yml" >}}
