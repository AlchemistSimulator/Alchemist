+++
title = "Nodes inside shapes"
weight = 5
tags = ["deployment", "node", "nodes", "shape", "random"]
summary = "Deployment of nodes randomly inside arbitrary shapes."
+++

Sometimes it is useful to deploy a bunch of nodes randomly inside some area marked by a shape.
Circles and polygons are first-class citizens,
but of course users may create their own deployments by implementing
{{% api package="model.deployments" class="Deployment" %}}.

This example places 1000 nodes randomly in a
{{% api package="model.deployments" class="Circle" %}}
with center in (0, 0) and radius 10.

{{<code path="src/test/resources/website-snippets/deployment-circle.yml" >}}

In the following example, they are instead deployed randomly within a 10x20
{{% api package="model.deployments" class="Rectangle" %}}
originating in (0,0).

{{<code path="src/test/resources/website-snippets/deployment-rectangle.yml" >}}

{{% api package="model.deployments" class="Polygon" %}}s
can be specified by providing all vertices.
In the following example, we deploy some nodes within the Venice lagoon.

{{<code path="src/test/resources/website-snippets/maps-simple.yml" >}}
