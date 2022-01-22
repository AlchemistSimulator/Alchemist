+++
title = "Node contents"
weight = 5
tags = ["deployment", "node", "nodes", "content", "molecule", "concentration"]
summary = "Definition of the initial content of nodes."
+++

It is possible to set the content of the nodes in a deployment.
Node contents are defined in terms of molecules and their corresponding concentration.
As such, they depend on the specific incarnation in use.

In the following example, we inject in all the nodes of a {{ anchor('Grid') }} deployment a molecule called `foo`  with
concentration `1`.
As stated before, it would only make sense if the incarnation supports integer concentrations and it's able to produce
a valid molecule from the `"foo"` String.

```yaml
deployments:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - molecule: foo
        concentration: 1
```

Multiple contents can be listed, e.g.,
if we want to also have a molecule named `bar` with value `0` along with `foo`,
we can just add another entry to the list:

```yaml
deployments:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - molecule: foo
        concentration: 1
      - molecule: bar
        concentration: 0
```

Molecules can be injected selectively inside a given {{ anchor('Shape') }}.
To do so, you can a filter with the `in keyword`.
In the following example, only the nodes inside the {{ anchor('Rectangle') }} area contain
the `source` molecule.

```yaml
deployments:
  - type: Grid
    parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - in:
          type: Rectangle
          parameters: [-6, -6, 2, 2]
        molecule: source
        concentration: true
```
