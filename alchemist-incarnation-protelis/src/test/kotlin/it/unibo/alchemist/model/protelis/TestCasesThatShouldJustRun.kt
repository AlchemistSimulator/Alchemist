/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.optional.bePresent
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import it.unibo.alchemist.test.AlchemistTesting.terminatingAfterSteps
import it.unibo.alchemist.util.ClassPathScanner

class TestCasesThatShouldJustRun :
    StringSpec(
        {
            ClassPathScanner.resourcesMatching(".+\\.ya?ml", "shouldrun").forEach {
                "simulation at ${it.path} should run" {
                    LoadAlchemist
                        .from(it)
                        .getDefault<Any, Nothing>()
                        .terminatingAfterSteps(100)
                        .runInCurrentThread()
                        .error shouldNot bePresent()
                }
            }
        },
    )
