+++
title = "Graphs"
weight = 6
tags = ["deployment", "node", "nodes", "graph", "graphstream", "scale free"]
summary = "Deployment of nodes into arbitrary graphs."
+++

![Arbitrary graphs](/images/simulator/graphstream.png)

Alchemist supports [Graphstream](https://graphstream-project.org/)-based deployments,
allowing for rich graphs to be used as node deployments.

Deployments of this kind can be instanced through
{{% api package="model.deployments" class="GraphStreamDeployment" %}}.

The most important parameter is the graph name, which must be a valid graph
[`Generator`](https://www.javadoc.io/doc/org.graphstream/gs-algo/latest/org/graphstream/algorithm/generator/Generator.html)
name in GraphStream.
If the generator's name ends in `Generator`, the last part can be omitted.
The trailing parameters are passed directly to the constructor of the generator.

In the following example, the deployment is used to generate a
[Lobster graph](https://doi.org/10.1016/j.ipl.2012.09.008):

{{< code path="alchemist-loading/src/test/resources/graphstream/testlobster.yml" >}}
