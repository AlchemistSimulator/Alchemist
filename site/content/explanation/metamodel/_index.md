+++
pre = ""
title = "The Alchemist Meta-Model"
weight = 1
tags = ["model", "metamodel", "understand", "molecule", "concentration", "node", "environment", "linking rule", "neighborhood", "reaction", "condition", "action"]
summary = "What does Alchemist simulate? A trip on the abstractions that populate the world of Alchemist."
+++

The first step to take in order to use the simulator, is to answer the question

> what does Alchemist simulate?

A broad introduction is provided in form of introductory video from the [DAIS 2021](https://www.discotec.org/2021/dais.html)
conference tutorial.

{{< youtube zF-LHHQjdOg >}}

### The model

The world of Alchemist is composed of the following entities:

* **{{% api package="model" class="Molecule" %}}**
    * The name of a data item
    * If Alchemist were an imperative programming language, a *molecule* would be the concept of *variable name*
* **{{% api package="model" class="Concentration" %}}**
    * The value associated to a particular *molecule*
    * If Alchemist were an imperative programming language, a *concentration* would be the concept of *value associated to a variable*
* **{{% api package="model" class="Node" %}}**
    * A container of *molecules* and *reactions*, living inside an *environment*
* **{{% api package="model" class="Environment" %}}**
    * The Alchemist abstraction for space. It contains *nodes* and provides:
        1. Where the nodes are in the space - i.e. their *position*
        2. The distance between two *nodes*
        3. Optionally, support for moving *nodes*
* **{{% api package="model" class="LinkingRule" %}}**
    * A function of the current status of the environment that associates to each *node* a *neighborhood*
* **{{% api package="model" class="Neighborhood" %}}**
    * An entity composed by a *node* (centre) and a set of *nodes* (neighbors)
* **{{% api package="model" class="Reaction" %}}**
    * A scheduled model operation that can change the state of the environment
    * Nodes and environments host possibly empty collections of reactions
    * Each reaction owns conditions, actions, and its absolute, observable next-occurrence time
    * A {{% api package="model" class="TimeDistributedReaction" %}} uses a
      {{% api package="model" class="TimeDistribution" %}} to generate delay samples and applies its own policy to
      samples and reactive model changes
    * An {{% api package="model.reactions" class="AbsoluteEvent" %}} instead owns one fixed absolute occurrence
    * [Reaction Scheduling and Ownership](/explanation/metamodel/reaction-scheduling/) explains reactive
      invalidation, scheduling transitions, specialized policies, and the boundary with the simulation engine
* **{{% api package="model" class="Condition" %}}**
    * A reactive prerequisite that exposes whether its owning reaction can execute
    * Reactions publish a finite occurrence while all their conditions are valid
    * Specialized reaction families may accept concrete condition types and read their domain state when deriving a rate
* **{{% api package="model" class="Action" %}}**
    * Models a change in the environment.

The following image is a visualization of such model:

![Alchemist model](/images/simulator/model.svg)

The behavior of the system is described in terms of reactions. As such, here's a pictorial representation of a reaction:

![Alchemist reaction](/images/simulator/reaction.svg)


### Incarnations

As you can see, names are given after classical chemistry terms.
This is mostly for historical reasons: Alchemist has been initially conceived as a chemical-oriented multi-compartment
stochastic simulation engine, able to support compartment (node) mobility while still retaining high performance.

Alchemist applies the same meta-model to broader domains through a deliberately loose interpretation of
**molecule** and **concentration**. These two terms have a very precise definition in chemistry, but in Alchemist they
are respectively

1. a generic identifier, and
2. a piece of data of some **type**

An {{% api package="model" class="Incarnation" %}} includes a **type** definition of **concentration**,
and possibly a set of specific conditions, actions and (rarely) environments and reactions that operate on such types.
In other words, an incarnation is a concrete instance of the Alchemist meta-model.
It also defines:

* Means for translating strings into named entities (molecules)
* Means for obtaining a number when given a node, a molecule and a string representing a property
* Means for building incarnation-specific model entities given an appropriate context and a parameter String

These functionalities are required in order to support a uniform access to different incarnations.

Different Incarnations can model completely different universes.
For instance, if the concentration is defined as a positive integer and proper actions and conditions are provided,
Alchemist becomes a stochastic simulator for chemistry featuring interconnected and mobile compartments.

The standalone distribution comes with:

* [Protelis Incarnation](https://protelis.github.io/)
* [SAPERE Incarnation](https://dx.doi.org/10.1016/j.pmcj.2014.12.002)
* Biochemistry Incarnation
* [Scafi incarnation](https://scafi.github.io/)
