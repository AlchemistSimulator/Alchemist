/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.test.AlchemistTesting.loadAlchemistFromResource
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import it.unibo.alchemist.test.AlchemistTesting.terminatingAfterSteps

class RegressionTest :
    StringSpec(
        {
            "reactions with neighbor outputs should execute" {
                loadAlchemistFromResource("it/unibo/alchemist/regressions/bug1718.yml")
                    .getDefault<Any, Nothing>()
                    .terminatingAfterSteps(100)
                    .runInCurrentThread()
                    .error
                    .ifPresent { throw it }
            }
        },
    )
