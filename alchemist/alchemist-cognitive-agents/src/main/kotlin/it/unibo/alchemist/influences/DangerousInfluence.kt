package it.unibo.alchemist.influences

import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.interfaces.Position2D

class DangerousInfluence<P : Position2D<P>>(
    x: Double,
    y: Double,
    radius: Double
) : BidimensionalGaussianLayer<P>(centerX = x, centerY = y, norm = 2.0, sigmaX = radius)