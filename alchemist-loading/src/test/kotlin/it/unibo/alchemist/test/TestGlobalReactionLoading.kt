/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSize
import it.unibo.alchemist.boundary.LoadAlchemist
import org.kaikikm.threadresloader.ResourceLoader

class TestGlobalReactionLoading :
    FreeSpec({
        "environment should contain a global reaction" {
            val environment =
                LoadAlchemist
                    .from(ResourceLoader.getResource("testGlobalReactionLoading.yml"))
                    .getDefault<Nothing, Nothing>()
                    .environment
            environment.globalReactions shouldHaveSize 1
        }
    })
