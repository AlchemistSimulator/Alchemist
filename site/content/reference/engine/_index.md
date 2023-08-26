+++
pre = ""
title = "Simulation Engine Configuration"
weight = 5
summary = "Available simulation engine configurations."
tags = ["configuration", "engine", "batch", "parallel"]
+++

## Engine Configuration

Default alchemist execution requires no special configuration,
however different engine implementations are available.

In order to configure a different engine, simply add an EngineConfiguration object into
the simulation configuration file as per the alchemist [Arbitrary class loading system](https://alchemistsimulator.github.io/reference/yaml/index.html).


### Parallel Batch Engines

Parallel batch engine is an implementaion of Alchemist's base engine 
that speeds up the computaitions by processing event batches
in parallel. **This implementation sacrifices determinism**.

All batch engine implementations require the following parameters:
- workersNumber - number of threads allocated to processing
- outputReplayStrategy - determines how the output monitors get notified after the 
batch has been processed. Available values:
    - aggregate - only the state after the batch processing is sent to the monitors 
    - replay - all the state changes get sent to the monitors ordered by scheduled time.

#### Fixed Size Batch Engine

Fixed size batch engine processes events in parallel in batches of fixed size.

Sample configuration:
```yaml
engine-configuration:
  type: FixedBatchEngineConfiguration
  parameters:
    outputReplayStrategy: aggregate
    workersNumber: 4
    batchSize: 4
```

For best results configure launcher to have workersNumber = batchSize.

#### Epsilon Batch Engine

Epsilon dynamic size batch engine processes events in parallel in batches 
constructed using the epsilon sensitivity value. Events get added to the batch
as long as the difference in scheduled time is lesser than the given epsilon value.

Sample configuration:
```yaml
engine-configuration:
  type: EpsilonBatchEngineConfiguration
  parameters:
    outputReplayStrategy: aggregate
    workersNumber: 4
    epsilonValue: 0.01
```

0.01 is a reasonable baseline, experiment to find the best value for your case.