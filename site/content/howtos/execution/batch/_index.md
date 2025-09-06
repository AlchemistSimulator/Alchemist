+++
title = "Parameter Sweeping with simulation batches"
weight = 1
tags = ["batch", "variables", "dry"]
summary = "Execute multiple instances of a simulation with different parameters"
+++

In some cases you may need to test the same simulation configuration
with different *parameters*. Suppose for example that you want to see what
happens when you place a bunch of pedestrian in a circle 
(for sake of simplicity we'll ignore their behavior).
You may want to 
observe the scenario with 50 pedestrians placed in a 5 meters radius circle.
Then you may like to observe it with 100 pedestrian and perhaps by changing 
the circle radius also. Instead of re-writing the configuration file over-and-over
for each parameter combination, Alchemist offers the possibility to write the
configuration once, and it will then derive a batch of simulations from the same configuration.

## Configuring batch simulations

To exploit this mechanism, you must declare the "parameters"
as **variables**. In our example, they would be the *number of pedestrian* and
the *radius of the circle* where to place them. Let's write the configuration file,
specifying that we want to test the simulation with 10, 30, 50, 70, 90 pedestrians
and a 5, 10, 15 meters circle radius:

{{< code path="src/test/resources/website-snippets/batch-pedestrian-simulation.yml" >}}

{{% notice info %}}
To understand how variables work refer to [this page](/howtos/simulation/variables/). 
You may also want to learn how to [export data](/howtos/simulation/export/) 
and specifying [termination criteria](/howtos/execution/termination/).
{{% /notice %}}

## Running batch simulations

### Using the launcher configuration (recommended)

The modern approach to running batch simulations is to configure the launcher directly 
in your simulation file. Add a `launcher` section specifying which variables to batch:

```yaml
# Your simulation configuration with variables
variables:
  nodeCount: &nodeCount
    type: LinearVariable
    parameters: [5, 5, 15, 10]  # Start: 5, Step: 5, End: 15, Default: 10
  range: &range
    type: LinearVariable  
    parameters: [1, 1, 3, 2]    # Start: 1, Step: 1, End: 3, Default: 2

# Configure the launcher for batch execution
launcher:
  parameters:
    batch: [nodeCount, range]

# Rest of your simulation configuration...
deployments:
  - type: Circle
    parameters: [*nodeCount, 0, 0, 5]
    # ... rest of configuration
```

Then run the simulation with:

```bash
java -jar ./build/shadow/alchemist-full-*-all.jar run simulation.yml
```

### Using command-line variable overrides

Alternatively, you can specify batch variables from the command line using the `--override` option:

```bash
java -jar ./build/shadow/alchemist-full-*-all.jar run simulation.yml --override "
launcher:
  parameters:
    batch: [nodeCount, range]
"
```

### Headless execution

For automated or server environments, use headless mode:

```bash
CI=true java -jar ./build/shadow/alchemist-full-*-all.jar run simulation.yml
```

### Understanding batch execution

Under the hood, the simulator will compute the cartesian product of all possible values 
of the variables specified in the `batch` list. Variables not included in the batch 
will use their default value.

For example, with:
- `nodeCount`: [5, 10, 15] (3 values)
- `range`: [1, 2, 3] (3 values)

The simulator will execute 3 × 3 = 9 different simulation configurations.

## Complete working example

Here's a complete simulation file that demonstrates batch execution with the modern launcher configuration:

{{< code path="src/test/resources/website-snippets/batch-complete-example.yml" >}}

This will execute 3 × 3 = 9 different simulation configurations:
- `nodeCount`: [5, 10, 15]
- `range`: [1, 2, 3]

Results will be exported to CSV files with the naming pattern `batch_results_*.csv`.
