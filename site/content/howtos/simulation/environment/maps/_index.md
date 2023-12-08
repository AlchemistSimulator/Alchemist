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
{{% api package="model.maps.environments" class="OSMEnvironment" %}}.

If you need map data to perform *on-streets routing*,
you need to feed it to the simulator.
{{% api package="model.maps.environments" class="OSMEnvironment" %}}
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
