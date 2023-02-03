+++
title = "Execute simulations batches"
weight = 1
tags = ["batch", "variables", "dry"]
summary = "Execute multiple instances of the same configuration"
+++

In some cases you may need to test the same simulation configuration
with different *parameters*. Suppose for example that you want to see what
happens when you place a bunch of pedestrian in a circle 
(for sake of semplicity we'll ignore their behavior). You may want to 
observe the scenario with 50 pedestrians placed in a 5 meters radius circle.
Then you may like to observe it with 100 pedestrian and perhaps by changing 
the circle radius also. Instead of re-writing the configuration file over-and-over
for each parameter combination, Alchemist offers the possibility to write the
configuration once, and it will then derive a batch of simulations.

## Launching batch simulations
To exploit this mechanism, you must declare what we above called "parameters"
as **variables**. In our example, they would be the *number of pedestrian* and
the *radius of the circle* where to place them. Let's write the configuration file,
specifing that we want to test the simulation with 10, 30, 50, 70, 90 pedestrian
and a 5, 10, 15 meters circle radius:

{{< code path="src/test/resources/website-snippets/batch-pedestrian-simulation.yml" >}}

{{% notice info %}}
To understand how variables work refer to [this page](/howtos/simulation/variables/). 
You may also want to learn how to deal with [data exportation](/howtos/simulation/export/) 
and [termination criterias](/howtos/execution/termination/).
{{% /notice %}}

Now we can launch the batch of simulations by yielding the following command:

```bash
./gradlew run --args="-y <path-to-simulation> -b -hl -var pedestrianNumber - var circleRadius -e <export-path>"
```

The `-b` option is to tell Alchemist that we want to execute a batch, while the `-hl` stands
for headless which means we are not interested in starting-up a gui.
Under the hood the simulator will compute the cartesian product of the variables sets
specified with the `-var` option.
