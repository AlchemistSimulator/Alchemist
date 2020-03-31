package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.HomogeneousPedestrian2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.influencesphere.FieldOfView2D
import org.apache.commons.math3.random.MersenneTwister

class TestSensory<T> : StringSpec({

    "field of view" {
        val env = Continuous2DEnvironment<T>()
        val rand = MersenneTwister(1)
        env.linkingRule = NoLinks()
        val observed = HomogeneousPedestrian2D(env, rand)
        val origin = Euclidean2DPosition(5.0, 5.0)
        env.addNode(observed, origin)
        val radius = 10.0
        origin.surrounding(env, radius).forEach {
            with(HomogeneousPedestrian2D(env, rand)) {
                env.addNode(this, it)
                env.setHeading(this, origin - it)
            }
        }
        env.nodes.minusElement(observed).forEach {
            with(FieldOfView2D(env, it, radius, Math.PI / 2).influentialNodes()) {
                size shouldBe 1
                first() shouldBe observed
            }
        }
    }
})
