/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.testsupport.createSimulation
import it.unibo.alchemist.testsupport.loadAlchemistFromResource
import it.unibo.alchemist.testsupport.runInCurrentThread

class RegressionTest : StringSpec(
    {
        "scheduled events should never crash the engine (bug #1718)" {
            loadAlchemistFromResource("it/unibo/alchemist/regressions/bug1718.yml")
                .getDefault<Any, Nothing>()
                .createSimulation()
                .runInCurrentThread()
                .error
                .ifPresent { throw it }
        }
    }
)
