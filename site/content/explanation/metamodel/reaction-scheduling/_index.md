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

* A `TimeDistribution` generates non-negative delay samples for a recurring `TimeDistributedReaction`. It does not
  know the environment, the engine, or a reaction's current schedule.
* A reaction owns its absolute, observable `nextOccurrence`. It decides when to draw a sample and when an existing
  occurrence must instead be preserved, transformed, or replaced.
* An `Event` owns one absolute occurrence directly. It has no time distribution or recurrence rate.
* A `ReactionHost` is a programmable model element.
    Both nodes and environments are reaction hosts and may contain any root `Reaction`.
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
The current transitional root protocol then invokes `updateSchedulingAfterFiring`, including when condition validity
prevented action execution, so a selected occurrence still advances stateful scheduling state without engine type
inspection. This hook will disappear once invalid conditions always make `nextOccurrence` infinite and cannot be
selected. An `Event` uses the same engine protocol, but its hook is a no-op: after its actions complete, `execute`
unregisters the event from its host. The host-neutral `Event` currently rejects conditions until
invalid-at-occurrence semantics are selected explicitly.

### Reactive invalidation

An observable model change can invalidate a reaction between occurrences.
The reaction refreshes its specialized state and applies an invalidation policy distinct from post-firing advancement.
If that policy changes `nextOccurrence`, the observable emits and the engine immediately asks the scheduler to
reindex the reaction.

The correct invalidation policy belongs to the reaction family:

* The base generic policy redraws a delay.
* An `AbstractMarkovianNodeReaction` preserves or rescales a surviving exponential residual after a positive rate
  change without drawing another random sample. Chemical reactions use this policy and reject non-exponential time
  distributions at construction.

### Membership and removal

Callers add and remove reactions through a `ReactionHost`, never by updating the scheduler separately.
The host changes membership first and, if a simulation is attached,
emits exactly one ordinary `reactionAdded` or `reactionRemoved` notification.
Adding a node emits `reactionAdded` once for each reaction already hosted by that node; removing a node emits the
corresponding `reactionRemoved` notifications before disposing the node.
The engine processes that notification on the simulation thread.
Removal disposes the exact scheduling subscription before removing the scheduler entry;
disposal is idempotent and prevents later scheduling emissions.

An `Event` invokes the same host removal operation after its actions complete.
The engine does not identify events or run an event-specific cleanup path.
Host-neutral reactions, including events, are not copied by node cloning;
only `NodeReaction` instances can be cloned when nodes are duplicated, via `cloneOnNewNode`.

## Engine boundary

The engine integrates a reaction into execution in a fixed order:

1. Complete reaction initialization and establish the current occurrence.
2. Insert the reaction into the scheduler.
3. Subscribe to `nextOccurrence`.
4. Reindex the reaction on every subsequent occurrence emission.
5. Invoke the temporary root `Reaction.updateSchedulingAfterFiring` transition after the selected occurrence.
6. Process host removal notifications from `ReactionHost`s.

Registration, occurrence emissions, scheduler updates, and scheduler removal are confined to the simulation thread.
Host mutations notify the engine through its command queue;
other mutations originating from another thread must be submitted through the same simulation scheduling API.

For the event loop, commands, monitors, and scheduler implementation, see
[the simulation engine explanation](/explanation/engine/).
