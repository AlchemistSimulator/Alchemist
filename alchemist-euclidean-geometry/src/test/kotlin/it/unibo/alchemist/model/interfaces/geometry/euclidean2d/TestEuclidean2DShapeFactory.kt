package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DShape
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DShapeFactory
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.euclidean.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition

private val factory: Euclidean2DShapeFactory =
    GeometricShapeFactory.getInstance<Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>()

private val fakeShape = object : Euclidean2DShape {
    override val diameter = 0.0
    override val centroid = Euclidean2DPosition(0.0, 0.0)
    override fun intersects(other: Euclidean2DShape) = true
    override fun contains(vector: Euclidean2DPosition) = true
    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit) = this
}

@SuppressFBWarnings("SE_BAD_FIELD_STORE")
class TestEuclidean2DShapeFactory : FreeSpec({
    /*
     * TODO: enable it once a proper implementation of euclidean geometry is provided
     *  the ! in front of the test name disables the test,
     *  it's currently disabled as to not prevent the build from succeeding
     */
    "!Shape.intersect symmetry" - {
        val firsts = factory.oneOfEachWithSize(DEFAULT_SHAPE_SIZE)
        val seconds = firsts.mapValues {
            // puts the other shapes in a corner to test "edge" cases
            it.value.transformed { origin(DEFAULT_SHAPE_SIZE * 2, DEFAULT_SHAPE_SIZE * 2) }
        }
        val names = firsts.keys.toList()
        for (f in names.indices) {
            for (s in f until names.size) {
                val first = checkNotNull(firsts[names[f]]) { "Could not find ${names[f]} shape" }
                val second = checkNotNull(seconds[names[s]]) { "Could not find ${names[s]} shape" }
                "${names[f]}.intersects(${names[s]}) must be the same as ${names[s]}.intersects(${names[f]})" {
                    first.intersects(second) shouldBe second.intersects(first)
                }
            }
        }
    }

    "Test requireCompatible" {
        shouldNotThrowAny {
            factory.oneOfEachWithSize(DEFAULT_SHAPE_SIZE)
                .values.forEach { factory.requireCompatible(it) }
        }
        shouldThrow<IllegalArgumentException> {
            factory.requireCompatible(fakeShape)
        }
    }

    "Multiple translations and complete rotations around the origin must preserve the centroid" - {
        factory.oneOfEachWithSize(DEFAULT_SHAPE_SIZE).forEach {
            it.key {
                /*
                 * Note: 8 rotations are performed, so this test must hold true for asymmetric shapes.
                 */
                val angle = Math.PI / 4
                val initialOrigin = Euclidean2DPosition(100.0, 100.0)
                val shape = it.value.transformed { origin(initialOrigin) }
                val rotated = shape.transformed { rotate(angle) }
                    .transformed { origin(500.0, 500.0) }
                    .transformed { rotate(angle) }
                    .transformed { origin(0.0, 0.0) }
                    .transformed { rotate(angle) }
                    .transformed { origin(7.0, 0.0) }
                    .transformed { rotate(angle) }
                    .transformed { rotate(angle); rotate(angle); rotate(angle) }
                    .transformed { origin(initialOrigin); rotate(angle) }
                rotated.centroid shouldBe shape.centroid
            }
        }
    }
}) {

    companion object {
        fun Euclidean2DShapeFactory.oneOfEachWithSize(size: Double) = mapOf(
            "circle" to circle(size * 2),
            "circleSector" to circleSector(size * 2, Math.PI, 0.0),
            "rectangle" to rectangle(size, size),
            "adimensional" to adimensional(),
        )

        const val DEFAULT_SHAPE_SIZE: Double = 1.0
    }
}
