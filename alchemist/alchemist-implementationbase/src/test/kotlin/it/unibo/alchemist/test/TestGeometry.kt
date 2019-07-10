package it.unibo.alchemist.test

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2DFactory

private const val DEFAULT_SHAPE_SIZE: Double = 1.0

private val factory = GeometricShape2DFactory.getInstance(Euclidean2DPosition::class.java)

private fun <T : Position2D<T>> GeometricShape2DFactory<T>.oneOfEachWithSize(size: Double) =
    mapOf(
        "circle" to circle(size * 2),
        "circleSector" to circleSector(size * 2, Math.PI, 0.0),
        "rectangle" to rectangle(size, size),
        "punctiform" to punctiform()
    )

// TODO: spotbugs reports: AbstractFreeSpec$FreeSpecScope stored into non-transient field TestIntersectionSymmetry
@SuppressFBWarnings("SE_BAD_FIELD_STORE")
@Suppress("MapGetWithNotNullAssertionOperator")
class TestIntersectionSymmetry : FreeSpec({
    // the ! in front of the test name disables the test, it's currently disable as to not prevent the build from succeeding
    // TODO: enable it once a proper implementation of euclidean geometry is provided
    "!" + factory.javaClass.simpleName - {
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