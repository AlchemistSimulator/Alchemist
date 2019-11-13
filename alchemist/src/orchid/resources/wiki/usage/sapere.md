---

title: "Alchemist SAPERE incarnation"

---


The SAPERE incarnation for Alchemist was the first stable incarnation produced for the simulator.
It was developed in the context of the [SAPERE EU project](http://archive.ph/umlcC).

At the core of [SAPERE](https://doi.org/10.1016/j.pmcj.2014.12.002) was the concept of *Live Semantic Annotation* (LSA),
namely a description of a resource (sensor, service, actuator...) always mapping the current resource status
(somewhat a prelude to the currently famous [digital twin](http://archive.ph/YR1v9) concept).

These annotations evolve following so-called *Eco-Laws*,
mimicking the complex behaviours of natural ecosystems.

The SAPERE approach fostered subsequent approaches, such as [aggregate computing](https://doi.org/10.1109/MC.2015.261).

## Live Semantic Annotations

An LSA as modeled in Alchemist is a tuple of values.
These tuples can be injected in nodes as data items.
From the point of view of {{anchor('the Alchemist metamodel', 'The Alchemist Simulator metamodel')}},
the concept of {{anchor('Molecule')}} is mapped to {{anchor('LSA', 'LsaMolecule')}}.
As a consequence, LSAs can be inserted in nodes.
For instance, the following code creates an irregular grid of devices,
of which those located around the center of such grid contain the tuple `{ token }`:

<script src="http://gist-it.appspot.com/github/AlchemistSimulator/SAPERE-incarnation-tutorial/blob/master/src/main/yaml/05-content.yml"></script>

The relevant part here is `molecule: token`.
If we wanted to inject the tuple `{ foo, 1, bar, 2 }`, we could have written `molecule: foo, 1, bar, 2`.

### Ground LSA syntax

```
GroundLSA ::= GroundArgument (',' GroundArgument)*
GroundArgument ::= Number | Atom | Set
Atom ::= [a-z]([a-z]|[A-Z]|[0-9])*
Number ::= [0-9]+('.'[0-9]*)
Set ::= '[' ((Atom | Number)';')* ']'
```

LSAs, similarly to Prolog terms, support [unification and substitution](http://archive.ph/oLSpq):
it is possible to create tuple templates,
match them against sets of ground tuples,
and obtain a matching ground tuple as result.

A tuple argument is considered a variable if it begins with an uppercase letter.
Additionally, it is possible to discard some matches by expressing constraints on values.

### LSA Syntax

```
LSA ::= '{' GroundLSA | TemplateLSA '}'
TemplateLSA ::= Argument (',' Argument)*
Argument ::= GroundArgument | Variable | Constraint
Variable ::= [A-Z]([a-z]|[A-Z]|[0-9])*
Constraint ::= 'def:' Variable Operation
Operation ::= ('>'|'>'|'='|'!=') Number | 'add ' Variable | 'del ' Variable
```

## Eco-Laws

Tuple matching is used to define *{{ anchor('Eco-Laws', 'SAPEREReaction') }}*.
An Eco-Law is a rewriting rule very similar in concept to chemical reactions:
elements on the left-hand side of the reaction are removed from the container,
elements on the right hand side are inserted instead.

The following program matches LSAs with two arguments, the former must be `foo`,
the latter a number greater than `30`, and produces in a new tuple having as first element `bar` and as second
the opposite of the matched number:

`{ foo, def: N > 30 } --> { bar, -N }`

Nodes can be programmed with Eco-Laws as follows:

<script src="http://gist-it.appspot.com/github/AlchemistSimulator/SAPERE-incarnation-tutorial/blob/master/src/main/yaml/06-send.yml"></script>

### Sharing

Eco-Laws can be programmed to send LSAs to neighbors, as well as to look into neighboring nodes for getting LSAs.
In order to do so, the LSA template in the Eco-Law must be preceded by a neighbor operator, either `+` or `*`.

`+` means *in a neighbor*:
if used on the left hand side,
it considers the condition satisfied if at least one neighbor has at least one LSA matching the provided template;
if used on the right hand side,
sends the LSA to one random neighbor.

`*` means *in all neighbors*:
if used on the left hand side,
it considers the condition satisfied if all neighbors have at least one LSA matching the provided template;
if used on the right hand side,
sends a copy of the LSA to all neighbors.

The following code exemplifies a diffusion program:
when `{ token }` is present locally, it is copied into neighboring nodes once per second;
and as soon as two copies of `{ token }` are present, one gets removed.

<script src="http://gist-it.appspot.com/github/AlchemistSimulator/SAPERE-incarnation-tutorial/blob/master/src/main/yaml/09-diffuse.yml"></script>

### Eco-Laws syntax


### Rates

The time distribution with which reactions should get scheduled can be controlled by thinkering with the yaml specification
as per every reaction in Alchemist.
If no {{anchor('time distribution', 'TimeDistribution')}} is specified,
the Eco-Law is assumed to run "as soon as possible" (ASAP).

This may lead to unwanted behaviour.
For instance, programming a single node with:
` --> { foo }`
will cause the simulation to schedule a reaction producing `{ foo }` at time zero,
and at each execution the time will remain zero:
the simulator will be producing copies over copies of the tuple,
never advancing in time (Alchemist is a discrete event simulator),
and possibly going on until the JVM memory limit is reached.

If a number is specified as time distribution, using the `time-distribution` key,
then it will be interpreted as the [Markovian rate](https://en.wikipedia.org/wiki/Markov_chain)
of an [exponentially distributed time](https://en.wikipedia.org/wiki/Exponential_distribution).

{{ anchor('Other distributions', 'it.unibo.alchemist.model.implementations.timedistributions') }} can be used leveraging the `type`/`parameters` syntax.

In the following example, two Eco-Laws are configured, and one of them is bound to an
{{ anchor('exponentially distributed time', 'ExponentialTime') }} with rate 1, namely, such reaction,
when executable (the left hand LSAs have local matches), will execute at an average of once per second
(with a variance of 1 s²).

<script src="http://gist-it.appspot.com/github/AlchemistSimulator/SAPERE-incarnation-tutorial/blob/master/src/main/yaml/06-send.yml"></script>

## Tutorial and exercises

To better grasp details of the incarnation, we recommend looking at the examples available on
[the Alchemist SAPERE Incarnation tutorial on GitHub](https://github.com/AlchemistSimulator/SAPERE-incarnation-tutorial).

Besides examples with growing complexity,
there are a number of proposed exercises that should help you getting acquainted with the SAPERE way of writing self-organizing behaviors.


