import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import it.unibo.alchemist.boundary.launch.Priority.Fallback
import it.unibo.alchemist.boundary.launch.Priority.High
import it.unibo.alchemist.boundary.launch.Priority.Normal

/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestPriorities : StringSpec({
    "priorities should get ordered correctly" {
        Normal shouldBeLessThan High("")
        Normal shouldBeGreaterThan Fallback("")
        Fallback("") shouldBeLessThan High("")
        High("") shouldBeGreaterThan Fallback("")
        Fallback("") shouldBeEqualComparingTo Fallback("other")
    }
})
