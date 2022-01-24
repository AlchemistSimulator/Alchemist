+++
title = "Maps and GPS traces"
weight = 5
tags = ["environment", "planimetry", "maps", "gps", "traces", "geo-spatial data"]
summary = "How to simulate using maps and GPS traces."
+++

---

Alchemist is equipped with the ability to load and simulate on real-world maps.
Navigation on maps can be done by using gps traces,
by moving along roads (Alchemist relies on [GraphHopper](https://www.graphhopper.com/) to provide directions),
by interpolating gps traces with on-the-road-movements,
or by ignoring the map information and just move as you would in a continuous space.

## Setting up a map environment

In order to run simulations on real world maps, an appropriate environment must be selected, such as
{{% api package="model.implementations.environments" class="OSMEnvironment" %}}.

If you need map data to perform *on-streets routing*,
you need to feed it to the simulator.
{{% api package="model.implementations.environments" class="OSMEnvironment" %}}
supports [OpenStreetMap](https://www.openstreetmap.org) extracts in several formats,
we recommend using the
[protocol buffer binary format (pbf)](https://wiki.openstreetmap.org/wiki/PBF_Format)
to save time and space.

{{% notice tip "OpenStreetMap extracts" %}}
It is likely that you do not need a simulation that requires navigation capabilities on
[the whole planet](https://planet.openstreetmap.org/),
especially considering that, even in binary format,
it contains **more than 50GB of data**.
We recommend thus to use an **extract** with the data relative to the area you are interested in simulating in.
One great way to obtain an extract is through [**BBBike**](https://extract.bbbike.org/).

If you rely on their service, consider donating to the project.
{{% /notice %}}

{{% notice tip "Deploying nodes using GPS traces" %}}
We prepared [a dedicated page](../../deploy/gps) on the argument
{{% /notice  %}}

{{% notice tip "Navigate nodes in map environment" %}}
We prepared [a dedicated page](../../program/move-on-maps/) on the argument
{{% /notice %}}



## Navigation

---

![indoor simulation](indoor_simulation.png)

Indoor environments
(bidimensional spaces with obstacles)
can be generated from images by leveraging
{{% api package="model.implementations.environments" class="ImageEnvironment" %}},
which loads the map as raster image from file,
interpreting the black pixels as obstacles
(wall-like areas not accessible to nodes).
Color of pixels that represents obstacles can be set to
every color with a constructor's parameter, black is default.

By default, each pixel is considered as a 1x1 block.
As a consequence, a 1200x600 image with a vertical line of black pixels at coordinate 500 will be interpreted as a single
obstacle of size 1x600 starting at coordinate (500, 0).
It is possible to scale up or down the size of the environment by acting on the zoom parameter of
{{% api package="model.implementations.environments" class="ImageEnvironment" %}},
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
