+++
title = "Parameter Sweeping with simulation batches"
weight = 1
tags = ["batch", "variables", "dry"]
summary = "Execute multiple instances of a simulation with different parameters"
+++

In some cases you may need to test the same simulation configuration
with different *parameters*. Suppose for example that you want to see what
happens when you place a bunch of pedestrian in a circle 
(for sake of semplicity we'll ignore their behavior).
You may want to 
observe the scenario with 50 pedestrians placed in a 5 meters radius circle.
Then you may like to observe it with 100 pedestrian and perhaps by changing 
the circle radius also. Instead of re-writing the configuration file over-and-over
for each parameter combination, Alchemist offers the possibility to write the
configuration once, and it will then derive a batch of simulations.the same configuration

## Launching batch simulations

To exploit this mechanism, you must declare the "parameters"
as **variables**. In our example, they would be the *number of pedestrian* and
the *radius of the circle* where to place them. Let's write the configuration file,
specifing that we want to test the simulation with 10, 30, 50, 70, 90 pedestrians
and a 5, 10, 15 meters circle radius:

{{< code path="src/test/resources/website-snippets/batch-pedestrian-simulation.yml" >}}

{{% notice info %}}
To understand how variables work refer to [this page](/howtos/simulation/variables/). 
You may also want to learn how to [export data](/howtos/simulation/export/) 
and specifying [termination criteria](/howtos/execution/termination/).
{{% /notice %}}

Now we can launch the batch of simulations by providing the simulator the following command-line arguments:
* `-b`: enable batch mode
* `-var pedestrianNumber`: all values of variable `pedestrianNumber` will be tested
* `-var circleRadius`: all values of variable `circleRadius` will be tested

Under the hood, the simulator will compute the cartesian product of the all possible values of the variables selected with the `-var` option.
Variables not selected for the batch will have their default value.
