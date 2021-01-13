package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.implementations.actions.LevyWalk
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.kaikikm.threadresloader.ResourceLoader

class TestLevyWalk : StringSpec() {
    init {
        "Test can load" {
            YamlLoader(ResourceLoader.getResourceAsStream("levywalk.yml"))
                .getDefault<Any, Euclidean2DPosition>()
                .nodes.first()
                .reactions.first()
                .actions.first()::class shouldBe LevyWalk::class
        }
    }
}
