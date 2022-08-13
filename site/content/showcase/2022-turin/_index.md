+++
pre = ""
title = "Simulation: 2017 Turin's stampede"
weight = 5
summary = "Simulation of Piazza San Carlo crowd disaster"
tags = ["simulation", "crowd", "mass", "disaster"]
hidden = true
+++

Simulation publicly available at [https://github.com/kelvin-olaiya/SanCarloSquareStampede](https://github.com/kelvin-olaiya/SanCarloSquareStampede)

## Abstract

The numerous tragedies, resulting from situations or events in the presence of crowds, 
which take place in every part of the world due to the inability to react in a coordinated manner 
to emergency situations, make it necessary to increase the understanding of the dynamics that emerge 
during the evacuation.

Many studies have analyzed what are the psychological aspects of the human being that come into play
in situations of panic. But it is also necessary to consider what happens on a physical level.

Take for example the Turin's stamped which took place at *Piazza San Carlo*, Turin, on June the 3rd 2017 in the 
occasion of the football's Champions Leaugue finals. It is believed it was a group of four robbers using pepper 
spray that caused the spread of panic in the crowd. The outcome of that tragic event was of over 1500 injured people and 2 deaths
This moved us to try and reproduce it, possibly for a better undestrandig on crowd dynamics. 

{{< youtube yuqcNgcgzIA >}}

First we introduced **cognitive agents** in the simulator to have a representation of the psicological aspects 
of a pedestrians such as *social contagion*. Then we gave those agents the ability of orienting themselves in an 
environment having different knoledge degrees of the environment. Finally, we introduced elements of physical 
micro-interaction in order to bring out typicall behaviors such as avoidance and pushing.


## Simulation description

For this simulation we placed over 40,000 cognitive nodes on a reproduction of **Piazza San Carlo's** map. 
These nodes represent adults and childs of male and female genders. 
After that, we placed a "danger zone" at the point where, analyzing the available footages of the tragedy, 
we deduced that the mass hysteria had begun. We also included rough representations of the barriers using fixed 
obstacles.

## Results

<!-- Simulation video -->

After executing the simulation we can observe how fear is spread amoung the crowd (social contagion). 
Fleeing pedestrian who have directly witnessed the danger are pushing the other who have not directly seen the danger.
This result is the formation of a "pushing wave" similar to the one visibile on the real disaster footage. 
Looking at some of the various exits from the Squar,e it also possible to observe the so-called 
[*Faster is slower* effect](https://doi.org/10.1038/35035023).

## Related readings

* [Cognitive agents](/explanation/cognitive)
* [Orienting agents](/explanation/pathfinding)
