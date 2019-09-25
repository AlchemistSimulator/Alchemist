package it.unibo.alchemist.test

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
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