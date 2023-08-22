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

- run - Tells that Alchemist simulation is to be runned
- --simulationFile - Indicates the resource or path to the resource for the simulation configuration file

### Logging Verbosity

Unless specifies, Alchemist logs with the "warn" logging level by default. Logging level tells
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

For example:

```cli
run --simulationFile simulation.yml --override 
```

### Launcher Configuration



### Migrating From Legacy CLI

### Common Launch Configurations
