package it.unibo.alchemist.test

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.actions.utils.surrounding
import it.unibo.alchemist.model.implementations.environments.EuclideanPhysics2DEnvironmentImpl
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.HomogeneousPedestrian2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.influencesphere.FieldOfView2D
import org.apache.commons.math3.random.MersenneTwister

class TestSensory<T> : StringSpec ({

    "field of view" {
        val env = EuclideanPhysics2DEnvironmentImpl<T>()
        env.linkingRule = NoLinks()
        val observed = HomogeneousPedestrian2D(env)
        val origin = Euclidean2DPosition(5.0, 5.0)
        env.addNode(observed, origin)
        val quantity = 5
        origin.surrounding(env, MersenneTwister(1), 5.0, quantity).forEach {
            with(HomogeneousPedestrian2D(env)) {
                env.addNode(this, it)
                env.setHeading(this, origin - it)
            }
        }
        env.nodes.minusElement(observed).forEach {
            with(FieldOfView2D(env, it, distance = 5.0).influentialNodes()) {
                size shouldBe 1
                first() shouldBe observed
            }
        }
    }
})