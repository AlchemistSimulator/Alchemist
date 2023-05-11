+++
title = "(Irregular) Grids"
weight = 5
tags = ["deployment", "node", "nodes", "grid"]
summary = "Deployment of nodes in (possibly irregular) grids."
+++

One common way to deploy nodes in a bidimensional space is on a grid.
Grid-like deployments can be easily performed in Alchemist
leveraging the {{% api package="model.deployments" class="Grid" %}} {{% api package="model" class="Deployment" %}}.

The following example shows a grid centered in `(0, 0)`,
with nodes distanced of `0.25` both horizontally and vertically.
{{<code path="src/test/resources/website-snippets/deployment-grid.yml" >}}

Often, symmetric structures may induce corner behaviors in self-organising systems,
and real-world "grid" deployments are not usually geometrically perfect.
Indeed, it is common to *perturb* the grid shape randomly,
in order to account for potential irregularities of the real-world system
being simulated.
{{% api package="model.deployments" class="Grid" %}} supports perturbation natively:
for instance, here is an example of a grid where positions are
randomly perturbed of ±0.1 distance units.

{{<code path="src/test/resources/website-snippets/deployment-grid-perturbed.yml" >}}
