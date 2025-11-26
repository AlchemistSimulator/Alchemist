package it.unibo.alchemist.dsl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Assertions.assertEquals

object LayerComparisonUtils {
    fun <T, P : Position<P>> compareLayerValues(dslEnv: Environment<T, P>, yamlEnv: Environment<T, P>) {
        println("Comparing layer values...")
        val samplePositions = mutableListOf<P>()
        samplePositions.addAll(dslEnv.nodes.map { dslEnv.getPosition(it) })
        samplePositions.addAll(yamlEnv.nodes.map { yamlEnv.getPosition(it) })
        val uniquePositions = samplePositions.distinct()
        if (uniquePositions.isNotEmpty()) {
            for (position in uniquePositions) {
                val dslLayerValues = dslEnv.layers.map { it.getValue(position) }
                val yamlLayerValues = yamlEnv.layers.map { it.getValue(position) }
                val dslDoubleValues = dslLayerValues.map { value ->
                    when (value) {
                        is Number -> value.toDouble()
                        else -> value.toString().toDoubleOrNull() ?: 0.0
                    }
                }
                val yamlDoubleValues = yamlLayerValues.map { value ->
                    when (value) {
                        is Number -> value.toDouble()
                        else -> value.toString().toDoubleOrNull() ?: 0.0
                    }
                }
                assertEquals(
                    dslDoubleValues,
                    yamlDoubleValues,
                    "Layer values at position $position should match",
                )
            }
        } else {
            println("Skipping layer value comparison - no valid positions found")
        }
    }
}
