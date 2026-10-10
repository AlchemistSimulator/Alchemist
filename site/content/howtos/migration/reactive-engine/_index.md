+++
title = "Migrating to the reactive engine"
weight = 1
tags = ["migration", "reaction", "condition", "action", "environment", "extension"]
summary = "How to adapt custom reactions, conditions, actions, environments, and incarnations to the reactive engine."
+++

This page is migration-oriented: it maps the APIs of the dependency-graph engine to their reactive replacements, for
those maintaining custom simulator extensions written against earlier releases.
The current model is described in the [metamodel](/explanation/metamodel/) and in the
[reaction scheduling](/explanation/metamodel/reaction-scheduling/) pages.

## Scheduling

* The engine no longer computes a dependency graph.
  Reactions own their schedule and publish it through the observable `nextOccurrence`
  of {{% api package="model" class="Reaction" %}}; the engine reindexes a reaction whenever it emits.
  `Context`, `Dependency`, input and output contexts, inbound and outbound dependencies,
  and `Condition.getDependencies()` no longer exist: a reaction subscribes to the observable model state it reads.
* The batch engine, its batched schedulers, and `EngineConfiguration` were removed:
  use {{% api package="core" class="Engine" %}}.
  Launcher parameter batches are still supported.
* {{% api package="model" class="TimeDistribution" %}} only samples delays (`sample()`) and creates fresh instances
  for cloned reactions (`newInstanceOn`); the reaction owns the occurrence time, the rate
  ({{% api package="model" class="TimeDistributedReaction" %}}), and the rescheduling policy.
* `Event` is now {{% api package="model.reactions" class="GenericReaction" %}}; one-shot behavior is provided by
  {{% api package="model.reactions" class="AbsoluteEvent" %}} and
  {{% api package="model.reactions" class="ConditionalEvent" %}}.
  Environment-wide reactions are ordinary reactions hosted by the environment, which, like nodes, is a
  {{% api package="model" class="ReactionHost" %}}.

## Reactions, conditions, and actions

* Reactions expose their fixed `host`, and hosts register only their own reactions.
* Conditions and actions expose the `reaction` owning them instead of their node;
  the host, and possibly the node, are reached through it.
  A reaction rejects conditions and actions owned by other reactions.
* Cloning takes the new owner only: `cloneCondition(newReaction)` and `cloneAction(newReaction)`.
* Extend the base class matching what the condition or action works on:
  * {{% api package="model.conditions" class="AbstractCondition" %}} and
    {{% api package="model.actions" class="AbstractAction" %}} for elements independent of the kind of host;
  * {{% api package="model.conditions" class="AbstractNodeCondition" %}} and
    {{% api package="model.actions" class="AbstractNodeAction" %}} for elements working on an explicit target node,
    which is the host of the new reaction after cloning if it was the host of the original one;
  * {{% api package="model.conditions" class="AbstractLocalCondition" %}} and
    {{% api package="model.actions" class="AbstractLocalAction" %}} for elements working on the node hosting their
    {{% api package="model" class="NodeReaction" %}}, which covers most of the former node-bound classes.
* Condition validity is the observable `isValid` property; install it with `setValidity` from model observables
  (concentrations, neighborhoods, positions, layers, ...).
  Conditions no longer contribute to propensities: specialized reactions read the typed inputs they need.
  `reactionReady` is now `beforeReactionFires`.
* Constructors take the owning reaction where they used to take the node.
  In YAML, the reaction is injected like the environment and the node;
  in the Kotlin DSL, the generated builders take it from the context.

## Model state

* Node contents, neighborhoods, positions, node counts, range queries, layer values, and host reactions are
  observable: consumers subscribe to them instead of polling.
  `Environment.nodes` is an observable list, read through `current`.
* Layers are associated with the environment during setup and cannot be added afterwards;
  their values may change over time, so read them through `Environment.observeLayerValue`.
* Node properties are part of the node setup: they cannot be added once the node is in an environment.
* Movement: `Environment.moveNodeTo` moves to an absolute position and `EuclideanEnvironment.moveNodeBy` by a
  displacement (formerly `moveNodeToPosition` and `moveNode`).
  Environments constraining movements override `moveNodeTo`, so their constraints apply to both.
* Java serialization is no longer supported by the model, the engine, and the loaders.
