+++
pre = ""
title = "2022: Turin's 2017 stampede in simulation"
weight = 5
summary = "Simulation of Piazza San Carlo crowd disaster"
tags = ["simulation", "crowd", "mass", "disaster"]
hidden = true
+++

Simulation publicly available at [https://github.com/kelvin-olaiya/SanCarloSquareStampede](https://github.com/kelvin-olaiya/SanCarloSquareStampede)

## Abstract

Understanding the dynamics that emerge during an emergency evacuation is paramount to better tackle
the disastrous events that can happen in overcrowded environments.

Many studies modeled the humans psychology in panicky situations,
our goal is to unify these findings with the physiscal interactions.

Our reference scenario is the stampede happened in Turin's *Piazza San Carlo* on June 3 2017.
A crowd assembled in front of a public screen to watch the UEFA Champions Leaugue final match (involving a local team).
At some point, an event of panic happened
(some sources report that some firecrackers went off,
others that a group of four robbers used pepper spray).
The panic resulted in over 1500 injured people and 2 casualties.

{{< youtube yuqcNgcgzIA >}}

First,
we introduced **cognitive agents** in the simulator to have a representation of the psicological aspects 
of a pedestrians such as *social contagion*.
Then, we gave those agents the ability of orienting themselves in an 
environment having different knoledge degrees of the environment.
Finally, we introduced elements of physical 
micro-interaction in order to bring out typicall behaviors such as avoidance and pushing.

## Simulation description

For this simulation we placed over 40,000 cognitive nodes on a reproduction of **Piazza San Carlo's** map. 
These nodes represent adults and childs of male and female genders. 
After that, we placed a "danger zone" at the point where, analyzing the available footages of the tragedy, 
we deduced that the mass hysteria had begun. We also included rough representations of the barriers using fixed 
obstacles.

## Results

{{< youtube owrbB-F12oQ >}}

After executing the simulation we can observe how fear is spread amoung the crowd (social contagion). 
Fleeing pedestrian who have directly witnessed the danger are pushing the other who have not directly seen the danger.
This result is the formation of a "pushing wave" similar to the one visibile on the real disaster footage. 
Looking at some of the various exits from the Squar,e it also possible to observe the so-called 
[*Faster is slower* effect](https://doi.org/10.1038/35035023).

## Related readings

* [Cognitive agents](/explanation/cognitive)
* [Orienting agents](/explanation/pathfinding)
