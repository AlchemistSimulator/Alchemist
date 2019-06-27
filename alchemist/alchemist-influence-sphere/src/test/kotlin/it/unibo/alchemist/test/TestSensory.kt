package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.influencesphere.sensory.HearingField2D
import it.unibo.alchemist.model.influencesphere.sensory.FieldOfView2D
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestSensory {

    @Test
    fun testFOV() {
        val fov = FieldOfView2D<Euclidean2DPosition>(0.0, 0.0, 90.0, 135.0, 5.0)
        Assertions.assertFalse(fov.isInfluenced(Euclidean2DPosition(-0.5, 5.0)))
        Assertions.assertTrue(fov.isInfluenced(Euclidean2DPosition(-4.0, 2.0)))
    }

    @Test
    fun testHearing() {
        val audioField = HearingField2D<Euclidean2DPosition>(3.0, 0.0, 3.0)
        Assertions.assertFalse(audioField.isInfluenced(Euclidean2DPosition(2.0, 3.0)))
        Assertions.assertTrue(audioField.isInfluenced(Euclidean2DPosition(0.1, 0.0)))
    }
}