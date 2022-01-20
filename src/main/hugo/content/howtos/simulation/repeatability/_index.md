+++
title = "Ensure repeatability"
weight = 1
tags = ["randomness", "reproducibility", "replicability", "seed", "random"]
summary = "Control randomness, ensuring reproducibility and replicability of experiments."
+++

Debugging a simulation requires the ability to reproduce the same behavior multiple times:
an unexpected behavior requiring investigation may happen far into the simulation,
or in corner conditions encountered by chance.
Randomness is controlled by setting the random generator seeds separately for the deployments and the simulation execution,
allowing for running different simulations on the same random deployment.
Seeds are set at the top level of the simulation specification.
