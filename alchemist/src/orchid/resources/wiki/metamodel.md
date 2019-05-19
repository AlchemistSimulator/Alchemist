---

javadoc:
  root: "../"
  base: "it/unibo/alchemist/"
  math3: http://commons.apache.org/proper/commons-math/javadocs/api-3.4/org/apache/commons/math3/

images: ../assets/media

---

## The world of Alchemist

The first step to take in order to use the simulator, is to answer the question

> what does Alchemist simulate?

### The model

The world of Alchemist is composed of the following entities:

* [**Molecule**]({{ javadoc.root }}{{page.javadoc.base}}model/interfaces/Molecule)
  * The name of a data item
  * If Alchemist were an imperative programming language, a *molecule* would be the concept of *variable name*
* [**Concentration**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Concentration)
  * The value associated to a particular *molecule*
  * If Alchemist were an imperative programming language, a *concentration* would be the concept of *value associated to a variable*
* [**Node**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Node)
  * A container of *molecules* and *reactions*, living inside an *environment*
* [**Environment**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Environment)
  * The Alchemist abstration for the space. It is a container for *nodes*, and it is able to tell:
    1. Where the nodes are in the space - i.e. their *position*
    2. The distance between two *nodes*
    3. Optionally, support for moving *nodes*
* [**Linking rule**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/LinkingRule)
  * A function of the current status of the environment that associates to each *node* a *neighborhood*
* [**Neighborhood**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Neighborhood)
  * An entity composed by a *node* (centre) and a set of *nodes* (neighbors)
* [**Reaction**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Reaction)
  * Any event that can change the status of the *environment*
  * Each *node* has a possibly empty set of *reactions*
  * Each reaction is defined by a possibly empty list of *conditions*, one or more *actions* and a [*time distribution*]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/TimeDistribution)
  * The frequency at which it happens depends on:
    1. A static "rate" parameter
    2. The value of each *condition*
    3. A "rate equation", that combines the static rate and the value of conditions, giving back an "instantaneous rate"
    4. A *time distribution*
* [**Condition**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Condition)
  * A function that takes the current *environment* as input and outputs a boolean and a number
  * If the *condition* does not hold (i.e. its current output is ``false``), the *reaction* to which it is associated cannot run
  * The outputed number may or may not influence the *reaction* speed (i.e. the average number of times the *reaction* "happens" per time unit), depending on the *reaction* and its *time distribution*.
* [**Action**]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Action)
  * Models a change in the environment.

The following image is a visualization of such model:

![Alchemist model]({{ images }}/model.png)

The behavior of the system is described in terms of reactions. As such, here's a pictorial representation of a reaction:

![Alchemist reaction]({{ images }}/reaction.png)


### Incarnations

As you can see, names are given after classical chemistry terms. This is mostly for historical reasons: Alchemist has been initially conceived as a chemical-oriented multi-compartment stochastic simulation engine, able to support compartment (node) mobility while still retaining high performance.

However, Alchemist is not limited to that. The key of its extensibility is in the very loose interpretation of **molecule** and **concentration**. These two terms have a very precise definition in chemistry, but in Alchemist they are respectively

1. a generic identifier, and
2. a piece of data of some **type**

An **incarnation** of Alchemist includes a **type** definition of **concentration**, and possibly a set of specific conditions, actions and (rarely) environments and reactions that operate on such types. In other words, an incarnation is a concrete instance of the Alchemist meta-model. In addition, a proper [Alchemist incarnation]({{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Incarnation.html) must also define:

* Means for translating strings into named entities (molecules)
* Means for obtaining a number when given a node, a molecule and a string representing a property
* Means for building incarnation-specific model entities given an appropriate context and a parameter String

These functionalities are required in order to support a uniform access to different incarnations.

Different Incarnations can model completely different universes. For instance, if the concentration is defined as a positive integer and proper actions and conditions are provided, Alchemist becomes a stochastic simulator for chemistry featuring interconnected and mobile compartments.

The standalone distribution comes with:

* [Protelis Incarnation](http://protelis.org)
* [SAPERE Incarnation](http://www.sapere-project.eu)
* [Biochemistry Incarnation]({{site.url}}/pages/tutorial/biochemistry)
* [Scafi incarnation](https://scafi.github.io/)

More details on how to use each of the included incarnations will be provided after this introductory chapter.

## The tool

The core part of the tool is the incarnation-agnostic simulation engine. Its current implementation is based on [Gibson and Bruck's Next Reaction](http://dx.doi.org/10.1021/jp993732q), extended to support addition and removal of reactions, and improved using input and output contexts for reactions, in order to prune the dependency graph as much as possible. More details on that are demanded to [this scientific paper on Journal of Simulation](http://dx.doi.org/10.1057/jos.2012.27).

The engine's entry point is the [simulation]({{ page.javadoc.root }}{{ page.javadoc.base }}core/interfaces/Simulation.html). It is equipped with support for commands like play, pause and stop, and can be equipped with an [output monitor]({{ page.javadoc.root }}{{ page.javadoc.base }}boundary/interfaces/OutputMonitor.html). The output monitor can be a graphical interface, a logger or any kind of environment inspector.

The tool also includes a [graphical interface developed in Java Swing][UI] and a [command line interface][CLI].

We suggest starting your journey into the Alchemist world by looking at [the next tutorial page: how to write simulations][Write simulations].

[Environment]: {{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Environment.html
[Node]: {{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Node.html
[Reaction]: {{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/Reaction.html
[TimeDistribution]: {{ page.javadoc.root }}{{ page.javadoc.base }}model/interfaces/TimeDistribution.html
[RandomEngine]: {{ page.javadoc.math3 }}random/RandomGenerator.html
[UI]: {{site.url}}/pages/tutorial/swingui
[CLI]: {{site.url}}/pages/tutorial/cli
[Write simulations]: {{site.url}}/pages/tutorial/simulations
