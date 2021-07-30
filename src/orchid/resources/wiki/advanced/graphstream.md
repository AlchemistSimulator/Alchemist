---

title: Graph-based deployments

---

Alchemist supports [Graphstream](https://graphstream-project.org/)-based deployments,
allowing for rich graphs to be used as node deployments.

Deployments of this kind can be instanced through the
[`GraphStreamDeployment`]({{ 'html/alchemist/it.unibo.alchemist.loader.deployments/-graph-stream-deployment/index.html' | baseUrl}}) type.

The most important parameter is the graph name, which must be a valid graph
[`Generator`](https://www.javadoc.io/doc/org.graphstream/gs-algo/latest/org/graphstream/algorithm/generator/Generator.html)
name in GraphStream.
If the generator's name ends in `Generator`, the last part can be omitted.
The trailing parameters are passed directly to the constructor of the generator.

In the following example, the deployment is used to generate a [Lobster graph](https://doi.org/10.1016/j.ipl.2012.09.008):

{{ snippet(file="testlobster.yml", from="alchemist-loading/src/test/resources/graphstream/") }}
