+++
pre = ""
title = "Command Line interface"
weight = 5
summary = "Available CLI options."
tags = ["cli", "command line", "options", "batch", "variables"]
+++

## The CLI Interface

Alchemist utilizes a CLI interface to run a simulation.

A minimal launch looks like this:

```cli
run --simulationFile simulation.yml
```

Where the options are

- `run` - Tells that Alchemist simulation is to be runned
- `--simulationFile` - Indicates the resource or path to the resource for the simulation configuration file

### Logging Verbosity

Unless specifies, Alchemist logs with the `warn` logging level by default. Logging level tells
how verbose and throrough the outputted logs are.

Alchemist has the following logging levels avaialble (from less to most verbose):

- off
- debug
- info
- warn
- error
- all

In order to specify verbosity, the `--verbosity` option can be used:

```cli
run --simulationFile simulation.yml --verbosity error
```

### Overriding Variables

Alchemist parses the configuration variables from the simulation configuration file.
In some cases it may be desirable to override some of the simulation file variables without
resorting to creating a new file. For such cases, `--override` option is available.
This options takes in input a valid yaml string representing the part of the configuration file to be overriden.

For example, given configuration file `simulation.yml`:

```yaml
foo:
  bar:
    fizz: 42
    buzz: some-string
```

And override with

```cli
run --simulationFile simulation.yml --override
foo:
    bar:
        buzz: 3
```

The resulting simulation file would be equivalent to

```yaml
foo:
  bar:
    fizz: 42
    buzz: 3
```

The overrides are arbitrary, types can be changed and new varibales introduced.

### Launcher Configuration

Alchemist needs a `Launcher` class in order to run the simulation. Unless configured,
Alchemist will default to a headless simulation launcher (`HeadlessSimulationLauncher`)
with default parameters.

If you would like to use another launcher class, you need to configure it in the simulation configuration file
as per the alchemist [Arbitrary class loading system](https://alchemistsimulator.github.io/reference/yaml/index.html).

Here is an example of a headless simulation run with additional parameters:

cli options

```cli
run --simulationFile simulation.yml
```

simulation.yml

```yaml
...
launcher:
  type: HeadlessSimulationLauncher
  parameters:
    parallelism: 4
    variables: [ 1, 2, 3, 4 ]
...
```

### Migrating From Legacy CLI

Here is a brief guide on how to re-map legacy CLI configuration options to the new
configuration flow.

- `-hl` - Migrated to launcher configuration, use HeadlessSimulationLauncher
- `-var` - Migrated to launcher configuration, used as parameters in supporting launchers
- `-b` - Migrated to launcher configuration
- `-fxui` - Migrated to launcher configuration
- `-d` - Migrated to launcher configuration
- `-g` - Migrated to launcher configuration, used as parameters in supporting launchers
- `-h` - Removed
- `-s` - Migrated to launcher configuration, used as parameters in supporting launchers
- `-p` - Migrated to launcher configuration, used as parameters in supporting launchers
- `-t` - Removed, use termination conditions instead (see examples below)
- `-y` - Removed, provide simulation file directly as program argument
- `-w` - Migrated to launcher configuration

### Common Launch Configurations Snippets

SwingGUI Launch configuration

```yaml
...
launcher:
  type: SingleRunSwingUI
  parameters:
    graphics: /effects/some-effect.json
...
```

Terminate after 50 time units configuration

```yaml
...
terminate:
  - type: AfterTime
    parameters: 50
...
```