+++
pre = ""
title = "2025: A Field-Based Approach for Runtime Replanning in Swarm Robotics Missions"
weight = 5
summary = "Experimental evaluation of a decentralized replanning mechanism grounded in Aggregate Computing to handle robot failures and environmental changes in real-time."
tags = ["simulation", "aggregate computing", "path replanning"]
hidden = true
+++

**Experimental Information**
Experimental artefact and simulation setup available at:
[https://github.com/angelacorte/experiments-2025-acsos-robots](https://github.com/angelacorte/experiments-2025-acsos-robots)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.14022712.svg)](https://doi.org/10.5281/zenodo.14022712)

This work was presented at the **2025 IEEE International Conference on Autonomic Computing and Self-Organizing Systems (ACSOS)**.
DOI: [10.1109/ACSOS66086.2025.00017](https://doi.org/10.1109/ACSOS66086.2025.00017)

**Technical Note**
This experiment utilizes the **Collektive** aggregate computing framework and the **Alchemist** simulator.

## Abstract
Ensuring mission success for multi-robot systems in unpredictable environments requires robust mechanisms to react to events like robot failures by adapting plans in real-time. Traditional methods often rely on centralized control, which is impractical for large teams or areas with unreliable network infrastructure where network segmentation is frequent.

This paper proposes a **field-based runtime task replanning approach** grounded in the Aggregate Computing (AC) paradigm. By representing the mission and environment as continuously evolving computational fields, robots can make decentralized decisions and collectively adapt ongoing plans. The core innovation lies in unifying **replanning triggers** (when to replan) and **plan synthesis** (how to replan) within a single field-based abstraction.

We explore two primary strategies:
* **Gossip-based approach**: Every robot independently monitors the collective state (active nodes and task status) and triggers replanning upon detecting changes.
* **Leader-based approach**: A leader is dynamically elected via distributed consensus to initiate and compute the replan, reducing the overall computational load.

The proposed approach is shown to scale linearly with team size and maintain performance levels close to an "oracle" centralized replanner.

## Experiment description
The evaluation aims to verify the resilience and scalability of the field-based approaches in a simulated Multi-Robot Task Allocation (MRTA) scenario. The mission requires a team of $m$ robots to service $n$ tasks, navigating from a source to a destination.

### Evaluated Strategies
1. **Gossip-based (G)**: Highly resilient; failures do not halt replanning, but it incurs higher computational overhead due to redundant calculations.
2. **Leader-based (L)**: Efficient; reduces system load by centralizing computation in one elected node per partition, but is vulnerable during leader re-election.
3. **Oracle (Baseline)**: A centralized controller with perfect, real-time global knowledge.
4. **Late-Stage (Baseline)**: Robots only check for uncompleted tasks after finishing their initial sequence.

### Parameters
* **Team Size ($m$)**: 5, 10, 20, 40 robots.
* **Task-to-Robot Ratio ($n/m$)**: 0.5, 1, 2, 4.
* **Communication Range ($R$)**: 20 m (sparse), 50 m, 100 m, and fully connected.
* **Failure Rate**: Mean time between failures ranging from 1,000s to 50,000s.

## Results
Performance is measured via **Mission Stable Time** (time to complete all tasks) and **Replanning Count** (overhead).

* **Communication Impact**: Connectivity is fundamental. With $R \ge 50$ m, field-based methods significantly outperform the Late-Stage baseline and approach Oracle performance.
* **Resilience**: The gossip-based approach is significantly more resilient to high failure rates than the leader-based approach.
* **Scalability**: Both field-based methods scale better than the Late-Stage baseline as robot and task counts increase. In large-scale scenarios ($m=40, n=160$), field-based methods completed missions ~30% faster than the baseline.

## Simulation Visualizations
The following sequence illustrates the swarm's behavior, showing how robots dynamically re-allocate tasks as failures occur:

![Swarm Robotics Replanning Simulation](./images/replanning.gif)

*In these simulations, red dots (tasks) turn green upon completion. Pink lines show robot trajectories, gray boxes indicate failed robots, and gray lines show communication links.*