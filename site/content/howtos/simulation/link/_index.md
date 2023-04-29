+++
title = "Create a network"
weight = 5
tags = ["network" , "linking rule", "network model", "connect", "connection", "neighborhood"]
summary = "Define how nodes should be connected with each other."
+++

Alchemist nodes can connect to each other and form a network.

The [`network-model`](/reference/yaml/#network-model) key is used to load the implementation of
{{% api class="LinkingRule" %}}
to be used in the simulation,
which determines the neighborhood of every node.

The key is optional, but defaults to {{% api package="model.implementations.linkingrules" class="NoLinks" %}},
so, if unspecified, nodes in the environment don't get connected.

Omitting the key is equivalent to writing any of the following:
```yaml
network-model:
  type: NoLinks
```
```yaml
network-model:
  type: NoLinks
  parameters: []
```

## Linking nodes based on their respective distance

One of the most common ways of linking nodes is to connect those which are close enough to each other.
To do so, you can use {{% api package="model.implementations.linkingrules" class="ConnectWithinDistance" %}},
passing a parameter representing the maximum connection distance.

Note that such distance depends on the environment: while the definition of distance is straightforward for euclidean spaces,
it's not so for [Riemannian manifolds](https://en.wikipedia.org/wiki/Riemannian_geometry),
which is a fancy name to define manifolds such as the one typical of a urban map
(you can roughly interpret it as a euclidean space "with holes").

For instance, in case of environments using
{{% api class="GeoPosition" %}}, the distance is computed in meters, so the
distance between `[44.133254, 12.237770]` and `[44.146680, 12.258627]` is about `2240` (meters).

In the following example, nodes are connected when closer than a threshold:
{{<code path="src/test/resources/website-snippets/deployment-in-three-points.yml" >}}
