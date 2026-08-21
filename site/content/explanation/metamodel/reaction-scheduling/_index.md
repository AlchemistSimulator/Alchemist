+++
pre = ""
title = "Reaction Scheduling and Ownership"
weight = 1
tags = ["model", "reaction", "condition", "time distribution", "scheduling", "reactive"]
summary = "How reactions turn model changes and time samples into observable occurrence times."
+++

Scheduling is part of the reaction model.
The simulation engine observes scheduling decisions, but it does not derive them from model state.

## Ownership

Scheduling responsibilities have explicit owners:

* A `TimeDistribution` generates non-negative delay samples. It does not know the environment, the engine, or a
  reaction's current schedule.
* A reaction owns its absolute, observable `nextOccurrence`. It decides when to draw a sample and when an existing
  occurrence must instead be preserved, transformed, or replaced.
* Conditions expose reactive validity and, where required by the current specialized APIs, propensity inputs.
  Model observables invalidate the reactions that consume them.
* The engine owns one exact subscription to every scheduled reaction's `nextOccurrence`.
* The scheduler indexes reactions by their current occurrence time.

The model and execution paths are therefore distinct:

```text
scheduling: model observable -> reaction policy -> nextOccurrence emission -> engine -> scheduler reindexing
firing: scheduler selection -> engine time advance -> reaction actions -> model mutation
```

## Reaction scheduling transitions

### Initialization

Once the environment is ready, the engine asks each reaction to complete initialization.
The reaction activates its reactive inputs, computes any specialized state, and establishes its first
`nextOccurrence` before the engine inserts it into the scheduler.
A reaction cloned onto another node is a newly instantiated program.

### Firing

The scheduler selects the reaction with the earliest occurrence and the engine advances simulation time to it.
If the reaction's conditions are valid, the engine notifies its conditions and executes its actions.
The selected occurrence is then advanced through the reaction's post-firing policy, including when condition
validity prevented action execution.

### Reactive invalidation

An observable model change can invalidate a reaction between occurrences.
The reaction refreshes its specialized state and applies an invalidation policy distinct from post-firing advancement.
If that policy changes `nextOccurrence`, the observable emits and the engine immediately asks the scheduler to
reindex the reaction.

The correct invalidation policy belongs to the reaction family:

* The base generic policy redraws a delay.
* An absolute-time `Trigger` is one-shot and does not move on reactive invalidation.
* An `AbstractMarkovianNodeReaction` preserves or rescales a surviving exponential residual after a positive rate
  change without drawing another random sample. Chemical reactions use this policy and reject non-exponential time
  distributions at construction.

### Removal

Runtime removal disposes the engine's scheduling subscription before removing the reaction from the scheduler.
The reaction is then disposed, releasing its own reactive subscriptions and preventing further scheduling emissions.

## Engine boundary

The engine integrates a reaction into execution in a fixed order:

1. Complete reaction initialization and establish the current occurrence.
2. Insert the reaction into the scheduler.
3. Subscribe to `nextOccurrence`.
4. Reindex the reaction on every subsequent occurrence emission.
5. On removal, dispose that exact subscription before removing and disposing the reaction.

Registration, occurrence emissions, scheduler updates, and removal are confined to the simulation thread.
Mutations originating from another thread must be submitted through the simulation command queue.

For the event loop, commands, monitors, and scheduler implementation, see
[the simulation engine explanation](/explanation/engine/).
