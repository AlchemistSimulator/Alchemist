/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.Alchemist

/**
 * Tests the execution of the Alchemist main. It triggers a VM exit and must get launched in its own JVM.
 */
class TestLaunchViaMain : StringSpec({
    "A simple simulation should be executable in headless mode" {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-t", "2"))
    }
})
