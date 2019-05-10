package it.unibo.alchemist.characteristics.utils

import kotlin.math.pow

class Functions {

    companion object {
        fun logistic(sigma: Double, tau: Double, vararg parameters: Double) =
                1 / (1 + Math.E.pow(-sigma * (parameters.sum() - tau)))

        fun advancedLogistic(sigma: Double, tau: Double, vararg parameters: Double) =
                logistic(sigma, tau, *parameters) - 1 / (1 + Math.E.pow(sigma*tau)) * (1 + Math.E.pow(-sigma*tau))
    }
}