package it.unibo.alchemist.test

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.Position

private const val DEFAULT_SHAPE_SIZE: Double = 1.0

private val factory = GeometricShapeFactory.getInstance(Euclidean2DPosition::class.java)

private fun <T : Position<T>> GeometricShapeFactory<T>.oneOfEachWithSize(size: Double) =
    mapOf(
        "circle" to circle(size * 2),
        "circleSector" to circleSector(size * 2, Math.PI, 0.0),
        "rectangle" to rectangle(size, size),
        "punctiform" to punctiform()
    )

@Suppress("MapGetWithNotNullAssertionOperator")
class TestIntersectionSymmetry : FreeSpec({
    factory.javaClass.simpleName - {
        val firsts = factory.oneOfEachWithSize(DEFAULT_SHAPE_SIZE)
        val seconds = firsts.mapValues {
            // puts the other shapes in a corner to test "edge" cases
            it.value.withOrigin(Euclidean2DPosition(DEFAULT_SHAPE_SIZE * 2, DEFAULT_SHAPE_SIZE * 2))
        }
        val names = firsts.keys.toList()
        for (f in 0 until names.size) {
            for (s in f until names.size) {
                val first = firsts[names[f]]!!
                val second = seconds[names[s]]!!
                "${names[f]}.intersects(${names[s]}) must be the same as ${names[s]}.intersects(${names[f]})" {
                    first.intersects(second) shouldBe second.intersects(first)
                }
            }
        }
    }
})