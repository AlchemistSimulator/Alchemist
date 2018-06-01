package it.unibo.alchemist.model.implementations.layers

import it.unibo.alchemist.model.implementations.utils.BidimensionalGaussian
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Position

/**
 * A [Layer] based on a [2D gaussian function][BidimensionalGaussian] and an optional baseline value.
 */
open class BidimensionalGaussianLayer @JvmOverloads constructor(
    private val baseline: Double = 0.0,
    centerX: Double,
    centerY: Double,
    norm: Double,
    sigmaX: Double,
    sigmaY: Double = sigmaX
) : Layer<Double> {

    /**
     * The function on which the layer is based.
     */
    val function = BidimensionalGaussian(norm, centerX, centerY, sigmaX, sigmaY)

    override fun getValue(p: Position) = baseline + function.value(p.getCoordinate(0), p.getCoordinate(1))
}