/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.euclidean.actions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import org.kaikikm.threadresloader.ResourceLoader

class TestLevyWalk : StringSpec() {
    init {
        "Test can load" {
            LoadAlchemist.from(ResourceLoader.getResource("levywalk.yml"))
                .getDefault<Any, Euclidean2DPosition>()
                .environment
                .nodes.first()
                .reactions.first()
                .actions.first()::class shouldBe LevyWalk::class
        }
    }
}
