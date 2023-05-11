+++
title = "GPS Traces"
weight = 6
tags = ["deployment", "node", "nodes", "gps", "traces", "gpx"]
summary = "Deployment of nodes on map-based environments using GPS data."
+++

{{% notice tip "Importing maps" %}}
GPS traces require a **geospatial environment**. We prepared [a dedicated page](../../environment/maps) on the topic.
{{% /notice  %}}

GPS traces can be used to deploy nodes on a map.
{{% api package="model.maps.deployments" class="FromGPSTrace" %}}
is a {{% api package="model" class="Deployment" %}}
that takes care of setting the initial position of the nodes depending the first position of the GPS traces.

The class supports deploying more nodes than there are available traces by reusing them cyclically.

{{< code path="alchemist-loading/src/test/resources/testgps.yml" >}}

{{% notice tip "Get GPS data for your experiments" %}}
The great folks at [OpenStreetMap](https://openstreetmap.org) release GPS data for
[the whole planet](https://planet.openstreetmap.org/gps/).
As per the map information,
regional extracts
are available
(the full data pack is otherwise larger than 50GB uncompressed).
{{% /notice %}}

### Alignment of time

Often, GPS traces are collected at different points in time.
When this is the case, a strategy must be concted to "align" them:
for instance, we may want all traces to be interpreted as beginning at the same time,
regardless of the actual time they were taken;
or we might want to discard the first hour of data;
or maybe we want to use them just as they are.

Alignment is performed by the subclasses of
{{% api package="boundary.gps" path="GPSTimeAlignment" %}}

The strategies available to align time of GPS trace are the following:

### {{% api package="boundary.gps.alignments" class="NoAlignment" %}}

No alignment is performed, traces are left untouched.

| Trace | Original time samples | Aligned time samples |
|-------|-----------------------|----------------------|
| A     | [2, 5]                | [2, 5]               |
| B     | [4, 6]                | [4, 6]               |

### {{% api package="boundary.gps.alignments" class="AlignToFirstTrace" %}}:

All traces get aligned to the start time of the first trace,
keeping their relative distance.

| Trace | Original time samples | Aligned time samples |
|-------|-----------------------|----------------------|
| A     | [2, 5]                | [0, 3]               |
| B     | [4, 6]                | [2, 4]               |

### {{% api package="boundary.gps.alignments" class="AlignToSimulationTime" %}}:

Aligns all traces to the initial simulation time,
not preserving relative time differences.

| Trace | Original time samples | Aligned time samples |
|-------|-----------------------|----------------------|
| A     | [2, 5]                | [0, 3]               |
| B     | [4, 6]                | [0, 2]               |

### {{% api package="boundary.gps.alignments" class="AlignToTime" %}}:

Aligns all traces with the given time in seconds from Epoch.
Discards all points before the provided epoch,
and shifts back all points located after that time to the initial
simulation time, preserving relative distances

| Trace | Provided Epoch | Original time samples | Aligned time samples |
|-------|----------------|-----------------------|----------------------|
| A     | 3              | [2, 5]                | [2]                  |
| B     | 3              | [4, 6]                | [1, 3]               |

## Examples

{{< code path="alchemist-loading/src/test/resources/testgps.yml" >}}
