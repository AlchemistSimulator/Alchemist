+++
pre = ""
title = "2024: Field-Based Coordination for Federated Learning"
weight = 6
summary = "Simulations demonstrating field-based and proximity-based approaches to decentralized federated learning in large-scale distributed systems."
tags = ["simulation", "federated learning", "aggregate computing", "scafi", "self-organization"]
hidden = true
+++

From {{< cite doi="10.48550/arXiv.2502.08577" >}} 
Simulations publicly available at:
- [https://github.com/domm99/experiments-2025-lmcs-field-based-FL](https://github.com/domm99/experiments-2025-lmcs-field-based-FL) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.17328680.svg)](https://doi.org/10.5281/zenodo.17328680)
- [https://github.com/domm99/experiments-2025-iot-self-federated-learning](https://github.com/domm99/experiments-2025-iot-self-federated-learning) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.17328674.svg)](https://doi.org/10.5281/zenodo.17328674)

## Abstract

In the era of pervasive devices and edge intelligence,
Federated Learning enables multiple distributed nodes to collaboratively train machine learning models
without exchanging raw data,
fostering privacy preservation and bandwidth efficiency.
However, real-world deployments face significant challenges:
data heterogeneity across devices,
dynamic network topologies,
and the lack of centralized control in truly distributed environments.

These simulations explore field-based and self-organizing coordination paradigms for federated learning,
built on top of the Aggregate Computing framework and implemented using ScaFi.
Through complementary experiments,
we investigate how devices can autonomously organize into federations,
aggregate model parameters,
and adapt to failures in a fully decentralized manner,
leveraging computational fields and spatial interaction patterns.

The following simulations are built on top of Alchemist.

## Simulation Descriptions

### Field-Based Federated Learning (FBFL)

This simulation explores the emergence of personalized model zones
using computational fields as a distributed coordination mechanism.
Devices in spatial proximity exchange model parameters through local diffusion processes,
forming hierarchical regions governed by dynamically elected leaders.
These leaders act as aggregators for their respective zones,
enabling localized learning and self-stabilizing coordination without central infrastructure.

The implementation leverages field coordination primitives from ScaFi,
including constructs for diffusion, convergence, and feedback loops.
Nodes autonomously select aggregators within their neighborhoods through distributed spatial leader election,
and aggregation occurs at multiple levels based on emergent spatial zones.

Key characteristics:
- **Datasets**: MNIST, FashionMNIST, Extended MNIST
- **Coordination**: Fully decentralized using computational fields
- **Aggregation**: Hierarchical, based on spatial proximity
- **Resilience**: Self-stabilizing under node failures and topology changes

The system achieves accuracy comparable to centralized FedAvg under IID settings,
while demonstrating superior robustness to dynamic network conditions.
Emergent spatial clusters naturally align with local data distributions,
creating personalized learning zones without explicit configuration.


## Snapshots

The following images show the evolution of self-organizing federations during a simulation
where aggregator failures occur and the system autonomously recovers.

<div style="text-align:center;white-space:nowrap;">
<img src="images/1.png" alt="start" style="display:inline-block;width:32%;margin:0 0.5%;vertical-align:top;" />
<img src="images/3.png" alt="stabilization" style="display:inline-block;width:32%;margin:0 0.5%;vertical-align:top;" />
<img src="images/5.png" alt="learning" style="display:inline-block;width:32%;margin:0 0.5%;vertical-align:top;" />
</div>
<div style="text-align:center;margin-top:0.5em;">
<small>Left: Start of the learning. Center: Federation stabilization. Right: Active learning phase.</small>
</div>

<div style="text-align:center;white-space:nowrap;margin-top:2em;">
<img src="images/6.png" alt="failure" style="display:inline-block;width:32%;margin:0 0.5%;vertical-align:top;" />
<img src="images/8.png" alt="recovery" style="display:inline-block;width:32%;margin:0 0.5%;vertical-align:top;" />
<img src="images/10.png" alt="resume" style="display:inline-block;width:32%;margin:0 0.5%;vertical-align:top;" />
</div>
<div style="text-align:center;margin-top:0.5em;">
<small>Left: Aggregator failures. Center: Federation re-stabilization. Right: Learning resumes.</small>
</div>

## Additional Resources

The work presented here is based on multiple publications:
- _Field-Based Coordination for Federated Learning_ at COORDINATION 2024 ([DOI: 10.1007/978-3-031-62697-5_4](https://doi.org/10.1007/978-3-031-62697-5_4))
- _Proximity-based Self-Federated Learning_ at ACSOS 2024 ([DOI: 10.1109/ACSOS61780.2024.00033](https://doi.org/10.1109/ACSOS61780.2024.00033))
- _FBFL: A Field-Based Coordination Approach for Data Heterogeneity in Federated Learning_ submitted to Logical Methods in Computer Science ([arXiv:2502.08577](https://doi.org/10.48550/arXiv.2502.08577))
- _Decentralized Proximity-Aware Clustering for Collective Self-Federated Learning_ submitted to Internet of Things journal

## Citation
{{< cite doi="10.48550/arXiv.2502.08577" style=bibtex >}}
