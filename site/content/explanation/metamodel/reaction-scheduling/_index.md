+++
pre = ""
title = "Reaction Scheduling and Ownership"
weight = 1
tags = ["model", "reaction", "condition", "time distribution", "scheduling", "reactive"]
summary = "How reactions turn model changes and time samples into observable occurrence times."
+++

Scheduling is part of the reaction model.
Each {{% api package="model" class="Reaction" %}} turns model state into an absolute, observable occurrence time.
The simulation engine subscribes to that occurrence and keeps its scheduler aligned with it.

## Ownership

Scheduling responsibilities have explicit owners:

* A {{% api package="model" class="TimeDistribution" %}} generates non-negative delay samples for a
  {{% api package="model" class="TimeDistributedReaction" %}}.
* A reaction owns its absolute, observable `nextOccurrence` and decides when to draw, preserve, transform, or
  replace a putative occurrence.
* An {{% api package="model.reactions" class="AbsoluteEvent" %}} owns one fixed absolute occurrence.
* A {{% api package="model.reactions" class="ConditionalEvent" %}} draws a new putative occurrence when its
  conditions become valid and unregisters itself after its first successful execution.
* A {{% api package="model" class="ReactionHost" %}} owns reaction membership. Both
  {{% api package="model" class="Node" %}} and {{% api package="model" class="Environment" %}} are reaction hosts.
* {{% api package="model" class="Condition" %}} exposes reactive validity through the general model contract.
  Specialized reaction families validate the concrete condition types they accept and read narrow semantic state from those types.
* Model observables invalidate the reactions that consume them.
* The engine owns one exact subscription to every scheduled reaction's `nextOccurrence`.
* The scheduler indexes reactions by their current occurrence time.

The model and execution paths are therefore distinct:

```text
scheduling: model observable -> reaction policy -> nextOccurrence emission -> engine -> scheduler reindexing
firing: scheduler selection -> engine time advance -> reaction procedure -> model mutation and/or removal
```

## Reaction scheduling transitions

### Initialization

Once the environment is ready, the engine asks each reaction to complete initialization.
The reaction activates its reactive inputs, computes any specialized state, and establishes its first
`nextOccurrence` before the engine inserts it into the scheduler.
A reaction is scheduled at an infinite future time while its conditions are invalid;
sampling begins on the transition to valid.
An {{% api package="model.reactions" class="AbsoluteEvent" %}} starts at its fixed occurrence and uses its conditions
as occurrence-time guards.
A reaction cloned onto another node is a newly instantiated program.

### Firing

The scheduler selects the reaction with the earliest occurrence and the engine advances simulation time to it.
The engine calls the selected reaction's `execute`;
the reaction owns condition, actions, recurrence, and single-use removal.
A scheduling-gated reaction exposes a finite occurrence while its conditions are valid.
At its fixed occurrence, an {{% api package="model.reactions" class="AbsoluteEvent" %}} evaluates its conditions,
applies its actions when all conditions are valid, and unregisters itself.
Successful recurring execution advances the reaction's stateful distribution.
A {{% api package="model.reactions" class="ConditionalEvent" %}} unregisters itself after its actions complete.

### Reactive invalidation

An observable model change can invalidate a reaction between occurrences.
The reaction refreshes its specialized state and applies an invalidation policy distinct from post-firing advancement.
If that policy changes `nextOccurrence`, the observable emits and the engine immediately asks the scheduler to
reindex the reaction.
An invalid reaction publishes the infinite occurrence. Sampling occurs when its scheduling policy enables it again.
Revalidation refreshes specialized state and applies the family policy from the current simulation time.

The correct invalidation policy belongs to the reaction family:

* The {{% api package="model.reactions" class="GenericReaction" %}} policy draws a fresh delay.
* A {{% api package="model.reactions" class="ConditionalEvent" %}} draws on each invalid-to-valid transition from
  the current simulation time. A later invalidation discards that occurrence and publishes infinity. Repeated valid
  updates preserve the pending occurrence.
* An {{% api package="model.reactions" class="AbstractMarkovianNodeReaction" %}} preserves or rescales a surviving
  exponential residual after a positive rate change and requires an
  {{% api package="model.timedistributions" class="ExponentialTime" %}}.

### Membership and removal

Callers add and remove reactions through a {{% api package="model" class="ReactionHost" %}}.
The host changes membership first and, if a simulation is attached,
emits exactly one ordinary `reactionAdded` or `reactionRemoved` notification.
Adding a node emits `reactionAdded` once for each reaction already hosted by that node; removing a node emits the
corresponding `reactionRemoved` notifications before disposing the node.
The engine processes that notification on the simulation thread.
Removal disposes the exact scheduling subscription before removing the scheduler entry;
disposal is idempotent and prevents later scheduling emissions.

Both event policies invoke the same host removal operation when their one-shot procedure completes.
Node cloning reconstructs each {{% api package="model" class="NodeReaction" %}} through `cloneOnNewNode`, including
a fresh conditional event and time distribution.

## Engine boundary

The engine integrates a reaction into execution in a fixed order:

1. Complete reaction initialization and establish the current occurrence.
2. Insert the reaction into the scheduler.
3. Subscribe to `nextOccurrence`.
4. Reindex the reaction on every subsequent occurrence emission.
5. Select and execute the earliest finite reaction.
6. Process membership notifications from {{% api package="model" class="ReactionHost" %}} implementations.

Registration, occurrence emissions, scheduler updates, and scheduler removal are confined to the simulation thread.
Host mutations notify the engine through its command queue;
other mutations originating from another thread must be submitted through the same simulation scheduling API.

For the event loop, commands, monitors, and scheduler implementation, see
[the simulation engine explanation](/explanation/engine/).
