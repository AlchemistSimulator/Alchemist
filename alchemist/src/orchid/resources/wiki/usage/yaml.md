---

title: "Writing Alchemist simulations"

---

As a first step, we recommend learning the YAML basics.
The language is so simple and human readable that there is probably no better way to learn it than to read it directly.
My suggestion is to use the tutorial "[Learn X in Y minutes where X = YAML](https://learnxinyminutes.com/docs/yaml/)", it should provide a good YAML guide (surely sufficient to follow the tutorial).

Alchemist expects a YAML map as input. In the following section, we'll discuss which keys it expects.
Of course, users are free to use all the YAML features (e.g. anchors) to organize their code and reduce duplication.

## Choosing an incarnation

The `incarnation` key is mandatory.
Actually, it's the only mandatory key.
The YAML parser expects a string value.
Such string will be used to get the most similarly named incarnation, namely the subclass of Incarnation whose simple name is closest to the string.

**Examples**

```yaml
incarnation: sapere
```

```yaml
incarnation: protelis
```

```yaml
incarnation: biochemistry
```

*Note:* this is also the most minimal valid alchemist specification

## Loading arbitrary Java classes with the `type`/`parameters` syntax

One important aspect of the Alchemist YAML is the ability to let the user control which actual Java classes should be
loaded inside a simulation, and which constructor should be used to do so.
Almost every entity of an Alchemist simulation can be instanced using arbitrary Java classes that implement the required
interfaces.
When the alchemist YAML parser encounters a YAML Map providing the keys `type` and `parameters`, it tries to resolve the
value of the value associated to `type` to a class name, then tries to create the object by calling the constructor with
parameters most suited to the value of `parameters`.

**Class name resolution**

The value associated with `type` must be a string representing a valid Java identifier.
If the value contains one or more `.` characters, then it will be interpreted as a fully qualified name.
If no such character is included, then *the default package for the desired alchemist entity will be prefixed*.
Alchemist won't ever attempt to load a class situated in the default package (so don't put your classes there, it's a
bad practice anyway).

**Object instancing**

If the class gets loaded correctly (meaning if a class is present in the classpath with the fully qualified name, whether it was passed or guessed by Alchemist), then its constructors get sorted based on the number and type of parameters.
The system tries to build an object with all the available constructors until one of them provides an instanced object, in an order that considers both the current context (namely, the entities that have already been instanced) and the value of `parameters`.

For instance, imagine that you are trying to build an instance of a Reaction, whose only constructor requires an Environment, a Node, an `int` and a `String`.
In this case, an Environment and a Node must have already been created (or the YAML loader won't be at this point).
As a consequence, the first two parameters are automatically inferred by the current context and passed to the constructor.
The other two parameters can not be inferred this way; instead, the value associated to `parameters` is used to extract the proper values (if possible).
In this case, this would have been a valid `parameters` entry:

```yaml
parameters: [4, foo]
```

As you can easily infer, the value of `parameters` must be a YAML list.

Don't despair if the class loading system is still unclear: it is used pervasively and it will become clearer with the examples in the next sections.

## Setting up the environment

The `environment` key is used to load the [Environment][Environment] implementation.
It is optional and it defaults to a [continuous bidimensional space][DefaultEnvironment].
If no fully qualified environment name is provided for class loading, Alchemist uses the package [environments][EnvironmentPackage] to search for the class.

**Examples**

The following simulations are equivalent, and load the default environment (which is incarnation independent, here `protelis` is picked, but it works for any other incarnation as well):
```yaml
incarnation: protelis
```
```yaml
incarnation: protelis
environment:
  type: Continuous2DEnvironment
```
```yaml
incarnation: protelis
environment:
  type: it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
```
```yaml
incarnation: protelis
environment:
  type: Continuous2DEnvironment
  parameters: []
```

The following simulation loads data from an Openstreetmap file (OSM, XML and PBF formats are supported) located in the classpath in the folder `maps`:
```yaml
incarnation: protelis
environment:
  type: OSMEnvironment
  parameters: [/maps/foo.pbf]
```

The following simulation loads data from a black and white raster image file located in the classpath in the folder `images` , interpreting the black pixels as obstacles (areas that cannot be accessed by nodes):
```yaml
incarnation: protelis
environment:
  type: ImageEnvironment
  parameters: [/images/foo.png]
```

The following simulation loads a personalized class named `my.package.FooEnv` implementing [Environment][Environment], whose constructor requires a String and a double:
```yaml
incarnation: protelis
environment:
  type: my.package.FooEnv
  parameters: [bar, 2.2]
```

More about the environments shipped with the distribution [here][Environments].

## Declaring variables

The `variables` section lists variable simulation values.
A variable defines some kind of value that can be referenced in the simulation file.

There are two kinds of variables: free and dependent variables.
The difference is that variables of the latter kind can always be computed given the values of all the other variables.

### Free variables

Free variables define a set of values and a default.
The value set is used to define the batch matrix, i.e., if the variable is selected for a batch, all its value are
included in the cartesian product that determines which simulations are to be executed.
If the simulation is not executed as batch, then the default value is used

#### Linear variables

#### Geometric variables

#### Arbitrary-valued variables

### Dependent variables

### Using variables

## Controlling the reproducibility

The `seeds` section may contains two optional values: `scenario` and `simulation`.
The former is the seed of the pseudo-random generator used during the creation of the simulation.
For instance, perturbating grid nodes in the `displacement` section.
The latter is the seed of the pseudo-random generator used during the simulation.
For instance, handling events concurrently (which event occurs before another).

**Examples**

Setting seeds with integer values.
```yaml
incarnation: protelis
seeds:
  scenario: 0
  simulation: 1
```

Setting seeds with variables.
```yaml
variables:
  random: &random
    min: 0
    max: 9
    step: 1
    default: 0
seeds:
  # reference to the `random` variable
  scenario: *random
  simulation: *random
```

## Defining the network

The `network-model` key is used to load the implementation of [linking rule][LinkingRule] to be used in the simulation.
It relies on the class loading mechanism, it is optional and defaults to [NoLinks][NoLinks] (nodes in the environment don't get connected).
Omitting such key is equivalent to writing any of the following:
```yaml
network-model:
  type: NoLinks
```
```yaml
network-model:
  type: it.unibo.alchemist.model.implementations.linkingrules.NoLinks
  parameters: []
```
```yaml
network-model:
  type: NoLinks
  parameters: []
```
If no fully qualified linking rule name is provided for class loading, Alchemist uses the package [linkingrules][LinkingRulesPackage] to search for the class.

**Example**
```yaml
network-model:
  type: EuclideanDistance
  # Link together all the nodes closer than 100 according to the euclidean
  # distance function
  parameters: [100]
```

## Displacing nodes

The `displacements` section lists the node locations at the beginning of the simulation. Each displacement type extends the interface [Displacement][Displacement]. If no fully qualified displacement name is provided for class loading, Alchemist uses the package [displacements][DisplacementPackage] to search for the class.

**Examples**

A single point located in (0, 0).
```yaml
displacements:
  # "in" entries, where each entry defines a group of nodes
  - in:
      type: Point
      # Using a constructor taking (x,y) coordinates
      parameters: [0, 0]
```

10000 nodes, placed in a circle with center in (0, 0) and radius 10.
```yaml
displacements:
  - in:
      type: Circle
      parameters: [10000, 0, 0, 10]
```

Nodes are randomly located in a square with a 0.1 distance units long side, centered in the point where the node was previously placed.
```yaml
displacements:
  - in:
      type: Grid
      parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
```

In order to specify a particular node implementation you want to put inside the environment you can use the `nodes` key
followed by the name of the class and the parameters required to build it.

100 `MyCustomNodeImpl` nodes, whose constructor needs only the environment, placed in a circle with center in (0, 0) and radius 20.
```yaml
displacements:
  - in:
      type: Circle
      parameters: [100, 0, 0, 20]
    nodes:
      type: MyCustomNodeImpl
      parameters: []
```


It is possible to set the content of the nodes inside a given region. Only the nodes inside the `Rectangle` area contain the `source` and `randomSensor` molecules (global variables).
```yaml
displacements:
  - in:
      type: Grid
      parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    contents:
      - in:
          type: Rectangle
          parameters: [-6, -6, 2, 2]
        molecule: source
        # Concentration = molecule value, any valid stateless protelis program is allowed
        concentration: true
        molecule: value
        # Java imports and method calls are allowed. Pay attention to randomness as
        # it breaks the reproducibility invariant of the simulation
        molecule: randomSensor
        concentration: >
          import java.lang.Math.random
          random() * pi
```

Nodes can execute a list of protelis programs.
```yaml
# Variable representing the program to be executed
gradient: &gradient
  - time-distribution: 1
    # Make sure that the program folder is part of the project classpath
    program: program:package:distanceTo
  - program: send
displacements:
  - in:
      type: Grid
      parameters: [-5, -5, 5, 5, 0.25, 0.25, 0.1, 0.1]
    programs:
      # Reference to the "gradient" list of programs. This program is executed in all
      # the grid nodes
      - *gradient
```


## Exporting data

The `export` section lists which simulation values are exported into the `folder` specified with the `-e path/to/folder` argument. Data aggregators are statistically univariate. Valid aggregating functions extend   [AbstractStorelessUnivariateStatistic].

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

[AbstractStorelessUnivariateStatistic]:http://commons.apache.org/proper/commons-math/javadocs/api-3.4/org/apache/commons/math3/stat/descriptive/AbstractStorelessUnivariateStatistic.html
