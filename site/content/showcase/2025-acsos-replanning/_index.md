+++
pre = ""
title = "2025: A Field-Based Approach for Runtime Replanning in Swarm Robotics Missions"
weight = 5
summary = "Experimental evaluation of a decentralized replanning mechanism grounded in Aggregate Computing to handle robot failures and environmental changes in real-time."
tags = ["simulation", "aggregate computing", "path replanning"]
hidden = true
+++
---
title: A Field-Based Approach for Runtime Replanning in Swarm Robotics Missions (2025)
summary: "Experimental evaluation of a decentralized replanning mechanism grounded in Aggregate Computing to handle robot failures and environmental changes in real-time."
---

Experimental artefact and simulation setup available at
[https://github.com/angelacorte/experiments-2025-acsos-robots](https://github.com/angelacorte/experiments-2025-acsos-robots)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.16578273.svg)](https://doi.org/10.5281/zenodo.16578273)

This work was presented at the **2025 IEEE International Conference on Autonomic Computing and Self-Organizing Systems (ACSOS)** (DOI: [10.1109/ACSOS66086.2025.00017](https://doi.org/10.1109/ACSOS66086.2025.00017)).

This article has won the **Best Student Paper Award** at [ACSOS 2025](https://acsos.github.io/awards/#Best-stu).

## Abstract
Ensuring mission success for multi-robot systems in unpredictable environments requires robust mechanisms to react to events like robot failures by adapting plans in real-time.
Adaptive mechanisms are especially needed for large teams deployed in areas with unreliable network infrastructure, where centralized control is impractical.

This paper proposes a **field-based runtime task replanning approach** grounded in the Aggregate Computing (AC) paradigm.
By representing the mission and environment as continuously evolving computational fields, robots can make decentralized decisions and collectively adapt ongoing plans.

The core of the approach focuses on two strategies:
- **Gossip-based approach**: Every robot independently monitors the collective state (active nodes and task status) and triggers replanning upon detecting changes.
- **Leader-based approach**: A leader is dynamically elected via distributed consensus to initiate and compute the replan, reducing the overall computational load.

The results show that the proposed approach scales linearly with team size and maintains performance levels close to an "oracle" centralized replanner,
provided the communication range is sufficient to maintain network connectivity.

## Experiment description
The evaluation aims to verify the resilience and scalability of the field-based approaches in a simulated Multi-Robot Task Allocation (MRTA) scenario.

The mission requires a team of $m$ robots to service $n$ tasks. Robots must navigate from a source to a destination while stopping at task locations.
The experiments are designed to test how the swarm reacts when robots fail or the environment changes.

We compared four main strategies:
- **Gossip Based**: Fully decentralized, high resilience but higher message overhead.
- **Leader Based**: Distributed leader election, lower overhead but dependent on leader stability.
- **Oracle**: A baseline with global knowledge (centralized).
- **Late-Stage**: A baseline where robots only check for missed tasks at the end of their plan.

The simulations varied several parameters:
* **Team Size ($m$)**: 5, 10, 20, 40 robots.
* **Task-to-Robot Ratio ($n/m$)**: 0.5, 1, 2, 4.
* **Communication Range ($R$)**: 20 m (sparse), 50 m, 100 m, and fully connected.
* **Failure Rate**: Mean time between failures ranging from 1,000s to 50,000s.

## Results
The experiments demonstrate that field-based replanning significantly improves mission efficiency compared to non-adaptive baselines.

Key findings include:
- **Connectivity**: A minimum communication range is required to allow the "fields" to propagate information.
  Once met, the performance gap between decentralized and centralized (Oracle) approaches becomes negligible.
- **Resilience**: The Gossip strategy proves most robust against high failure rates, as it does not rely on specific nodes (like leaders) to coordinate.
- **Efficiency**: In large-scale scenarios, the field-based approach reduces the time to mission completion by up to 30% compared to the Late-Stage baseline.

### Simulation Sequence
The following animation shows the swarm in action, where robots dynamically re-allocate tasks as failures occur or tasks are completed:

![Swarm Robotics Replanning Simulation](./images/replanning.gif)

*In the visualization, red dots represent active tasks, green dots represent completed tasks, and gray squares indicate failed robots. 
The pink lines show the paths currently planned by the aggregate logic.*