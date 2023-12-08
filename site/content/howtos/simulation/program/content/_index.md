+++
title = "Node contents"
weight = 5
tags = ["deployment", "node", "nodes", "content", "molecule", "concentration"]
summary = "Definition of the initial content of nodes."
+++

It is possible to set the content of the nodes in a deployment.
Node contents are defined in terms of molecules and their corresponding concentration.
As such, they depend on the specific {{% api class="Incarnation" %}} in use.

This is done by listing the contents under [`deployments.contents`](/reference/yaml/#deploymentcontents),
specifying a {{% api class="Molecule" %}} name and its {{% api class="Concentration" %}}.

Unless the [type/parameter syntax](/reference/yaml/#arbitrary-class-loading-system) is used, the data gets processed 
by the
{{% api class="Incarnation" %}}
through the
{{% api class="Incarnation" method="createMolecule" %}}
and
{{% api class="Incarnation" method="createConcentration" %}}
methods, respectively.

In the following example, three molecules are created and injected into all nodes deployed in the scenario:

{{<code path="alchemist-incarnation-protelis/src/test/resources/gradient.yml" >}}

By default, all nodes in the deployment will be injected with the required contents.
It is possible, though, to select only a subset of them through the [`in`](/reference/yaml/#contentin) keyword,
which expects enough information to be able to build a
{{% api package="model" class="PositionBasedFilter" %}}
through the [arbitrary class loading system](/reference/yaml/#arbitrary-class-loading-system).

In the following example, only molecules located inside a
{{% api package="model.positionfilters" class="Rectangle" %}}
get the `ball` molecule:

{{<code path="src/test/resources/website-snippets/grid-dodgeball.yml" >}}
