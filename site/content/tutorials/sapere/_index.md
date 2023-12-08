+++
title = "SAPERE Incarnation Tutorial"
weight = 5
tags = ["sapere", "lsa", "tuple", "tuple space", "tuple centre", "tutorial"]
summary = "Ready-to-run examples of increasing complexity with the SAPERE incarnation"
+++

An explanation of the basics of the SAPERE Incarnation is available [here](/explanation/sapere).
A tutorial similar to the [base tutorial](../basics), with increasingly rich examples,
focused on the SAPERE incarnation.
Reference descriptions of the SAPERE LSA language inside the simulator are available
[here](/reference/sapere)

The tutorial can be found on [GitHub](https://github.com/AlchemistSimulator/Sapere-Incarnation-tutorial).
The `README.md` file of the project explains the use and the steps to follow.

{{% expand "Show README.md" %}}
{{% github repo="Sapere-Incarnation-tutorial" highlight="false" %}}
{{% /expand %}}

{{% notice note %}}
Something went wrong along the line? Drop us an
[issue report](https://github.com/AlchemistSimulator/Sapere-Incarnation-tutorial/issues/new/choose)
and we'll get back to you.
{{% /notice %}}

## LSAs

[Syntax details are available in the reference](/reference/sapere#lsa-syntax).
The following code creates an irregular grid of devices,
of which those located around the center of such grid contain the tuple `{ token }`:

{{< github repo="SAPERE-incarnation-tutorial" path="src/main/yaml/05-content.yml" >}}

The relevant part here is `molecule: token`.
If we wanted to inject the tuple `{ foo, 1, bar, 2 }`, we could have written `molecule: foo, 1, bar, 2`.

## Eco-Laws

Nodes can be programmed with Eco-Laws as follows:

{{< github repo="SAPERE-incarnation-tutorial" path="src/main/yaml/06-send.yml" >}}

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

{{< github repo="SAPERE-incarnation-tutorial" path="src/main/yaml/09-diffuse.yml" >}}

### Rates

The time distribution with which reactions should get scheduled can be controlled by thinkering with the yaml specification
as per every reaction in Alchemist.
If no
{{% api class="TimeDistribution" %}}
is specified,
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

Other distributions found at
{{% api package="model.timedistributions" %}}
can be used leveraging the
[arbitrary class loading system](/reference/yaml#arbitrary-class-loading-system).

In the following example, two Eco-Laws are configured, and one of them is bound to an
{{% api package="model.timedistributions" class="ExponentialTime" %}}
with rate 1, namely, when the reaction can be executed
(the left hand LSAs have local matches),
it will execute at an average of once per second
(with a variance of 1 s²).

{{< github repo="SAPERE-incarnation-tutorial" path="src/main/yaml/06-send.yml" >}}

## Exercise

To better grasp details of the incarnation, we recommend looking at the examples available on
[the Alchemist SAPERE Incarnation tutorial on GitHub](https://github.com/AlchemistSimulator/SAPERE-incarnation-tutorial).

Besides examples with growing complexity,
there are a number of proposed exercises that should help you get acquainted with the SAPERE way of writing self-organizing behaviors.
