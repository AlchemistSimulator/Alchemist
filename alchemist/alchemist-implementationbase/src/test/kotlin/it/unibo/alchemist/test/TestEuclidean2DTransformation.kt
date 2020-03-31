package it.unibo.alchemist.test

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.geometry.AdimensionalShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShapeFactory

private val factory: Euclidean2DShapeFactory = GeometricShapeFactory.getInstance()

@SuppressFBWarnings("SE_BAD_FIELD_STORE")
class TestEuclidean2DTransformation : FreeSpec({
    "Test origin" - {
        factory.oneOfEachWithSize(DEFAULT_SHAPE_SIZE)
            .filter { it.value !is AdimensionalShape }
            .forEach {
                it.key {
                    var shape = it.value.transformed {
                        origin(0.0, 0.0)
                    }
                    val reference = shape.centroid
                    shape = shape.transformed {
                        origin(0.0, 0.0)
                    }
                    reference.getDistanceTo(shape.centroid) shouldBe 0.0
                    shape = shape.transformed {
                        origin(0.0, 10.0)
                    }
                    reference.getDistanceTo(shape.centroid) shouldBe 10.0
                    shape = shape.transformed {
                        origin(10.0, 10.0)
                        origin(3.0, 3.0)
                        origin(6.0, 0.0)
                    }
                    reference.getDistanceTo(shape.centroid) shouldBe 6.0
                }
            }
    }
})
