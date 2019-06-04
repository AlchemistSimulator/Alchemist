---

title: "Exporting data"

---

## Defining exported data

Data expected as output can be exported by telling Alchemist what to log, and by passing to the executable information
on where to export.

The `export` section lists which simulation values are exported into the `folder` specified with the `-e path/to/folder`
argument.

### Aggregating data

Data aggregators are statistically univariate.
Valid aggregation functions must extend [AbstractStorelessUnivariateStatistic].

**Examples**
```yaml
export:
  # Time step of the simulation
  - time
  # Number of nodes involved in the simulation
  - number-of-nodes
  # Molecule representing an aggregated value
  - molecule: danger
    aggregators: [sum]
```

### Filtering unwanted values

TODO

[AbstractStorelessUnivariateStatistic]:http://commons.apache.org/proper/commons-math/javadocs/api-3.4/org/apache/commons/math3/stat/descriptive/AbstractStorelessUnivariateStatistic.html
