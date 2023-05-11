+++
title = "Move nodes on maps"
weight = 5
tags = ["reaction", "program", "behavior", "maps", "gps", "interpolation", "geospatial"]
summary = "How to move node around in geospatial environments."
+++

There are several possibilities to move nodes in gep-spatial environment.

## Ignore geo-spatial information

![orthodromes](no_map_information.png)

This is of course the easiest way:
all data about the map is ignored.
This strategy makes sense if you need a geo-spatial coordinate system,
but you are simulating objects that are mostly or entirely unaffected by the street-level structure
(buildings, roads, etc.);
for instance, if the simulation involves unmanned aerial vehicles.
There is no need of [importing actual map data](../../environment/maps) when navigating this way.

This kind of navigation can be realized using
{{% api package="model.actions" class="MoveToTarget" %}}.

### Examples

{{< code path="alchemist-incarnation-protelis/src/test/resources/18-export.yml" >}}

{{< code path="alchemist-incarnation-protelis/src/test/resources/tomacs.yml" >}}

**From the showcase**

[Optimal resilient distributed data collection in mobile edge environments](/showcase/2020-jcee)

## Navigate along the streets

![navigate along the streets](use_map_information.png)

Moves along the available paths, depending on the specific vehicle being used.
Requires actual geo-spatial information.

This kind of navigation can be realized using
{{% api package="model.maps.actions" class="TargetMapWalker" %}}.

## Reproduce a GPS Trace

![reproduce a GPS trace](reproduce_gps_trace.png)

Ignores the map geospatial information and relies on a GPS trace instead,
starting from its first position and reaching the last,
navigating from point to point in "straight lines"
(on maps, these are actually [orthodromes](https://en.wikipedia.org/wiki/Great-circle_distance)).

This kind of navigation can be realized using
{{% api package="model.maps.actions" class="ReproduceGPSTrace" %}}.

{{% notice tip "Deploying nodes using GPS traces" %}}
You probably want your nodes to *start* from the position that marks the beggining of a trace.
We discussed [how to do so here](/howtos/simulation/deploy/gps).
{{% /notice %}}

{{% notice warning "Time alignment of GPS traces" %}}
Navigation with GPS traces usually require that they get correctly aligned with time,
especially if they come from samples taken at different times.
We discussed the alignment of GPS traces [here](/howtos/simulation/deploy/gps/#alignment-of-time),
the same alignment system used for importing traces is used for using them during navigation.
{{% /notice %}}

### Examples

{{< code path="alchemist-loading/src/test/resources/testgps.yml" >}}

### Interpolate GPS traces with street data

![interpolate GPS traces](interpolate_gps_trace_with_street_data.png)

Navigates along a GPS trace, but computes the point-to-point distance using the navigation system,
rather than "straight lines" ([orthodromes](https://en.wikipedia.org/wiki/Great-circle_distance)).

This kind of navigation can be realized using
{{% api package="model.maps.actions" class="GPSTraceWalker" %}}.
