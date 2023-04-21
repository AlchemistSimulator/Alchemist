/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.Exporter
import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader

class TestExportersCreation<T, P : Position<P>> : StringSpec({
    "test loading exporters" {
        val file = ResourceLoader.getResource("testExporters.yml")
        assertNotNull(file)
        val loader = LoadAlchemist.from(file)
        assertNotNull(loader)
        val initialized: InitializedEnvironment<T, P> = loader.getDefault()
        val exporters: List<Exporter<T, P>> = initialized.exporters
        exporters.size shouldBeGreaterThan 0
        exporters.forEach {
            it shouldNotBe null
        }
        exporters.forEach {
            it.dataExtractors.size shouldBeGreaterThan 0
        }
    }
})
