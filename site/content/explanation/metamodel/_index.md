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

* **Molecule**
    * The name of a data item
    * If Alchemist were an imperative programming language, a *molecule* would be the concept of *variable name*
* **Concentration**
    * The value associated to a particular *molecule*
    * If Alchemist were an imperative programming language, a *concentration* would be the concept of *value associated to a variable*
* **Node**
    * A container of *molecules* and *reactions*, living inside an *environment*
* **Environment**
    * The Alchemist abstration for the space. It is a container for *nodes*, and it is able to tell:
        1. Where the nodes are in the space - i.e. their *position*
        2. The distance between two *nodes*
        3. Optionally, support for moving *nodes*
* **Linking rule**
    * A function of the current status of the environment that associates to each *node* a *neighborhood*
* **Neighborhood**
    * An entity composed by a *node* (centre) and a set of *nodes* (neighbors)
* **Reaction**
    * Any event that can change the status of the *{{ anchor('environment', 'Environment') }}*
    * Each *node* has a possibly empty set of *reactions*
    * Each reaction is defined by a possibly empty list of *conditions*, one or more *actions* and a *{{ anchor('time distribution', 'TimeDistribution') }}*
    * The frequency at which it happens depends on:
        1. A static "rate" parameter
        2. The value of each *condition*
        3. A "rate equation", that combines the static rate and the value of conditions, giving back an "instantaneous rate"
        4. A *time distribution*
* **Condition**
    * A function that takes the current *environment* as input and outputs a boolean and a number
    * If the *condition* does not hold (i.e. its current output is ``false``), the *reaction* to which it is associated cannot run
    * The outputed number may or may not influence the *reaction* speed (i.e. the average number of times the *reaction* "happens" per time unit), depending on the *reaction* and its *time distribution*.
* **Action**
    * Models a change in the environment.

The following image is a visualization of such model:

![Alchemist model](/images/simulator/model.svg)

The behavior of the system is described in terms of reactions. As such, here's a pictorial representation of a reaction:

![Alchemist reaction](/images/simulator/reaction.svg)


### Incarnations

As you can see, names are given after classical chemistry terms.
This is mostly for historical reasons: Alchemist has been initially conceived as a chemical-oriented multi-compartment
stochastic simulation engine, able to support compartment (node) mobility while still retaining high performance.

However, Alchemist is not limited to that. The key of its extensibility is in the very loose interpretation of
**molecule** and **concentration**. These two terms have a very precise definition in chemistry, but in Alchemist they
are respectively

1. a generic identifier, and
2. a piece of data of some **type**

An **incarnation** of Alchemist includes a **type** definition of **concentration**,
and possibly a set of specific conditions, actions and (rarely) environments and reactions that operate on such types.
In other words, an incarnation is a concrete instance of the Alchemist meta-model.
In addition, a proper Alchemist incarnation', 'Incarnation must also define:

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
