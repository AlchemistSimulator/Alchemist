/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith

class TestCopernicusRequest : StringSpec({

    // a realistic request to the EWDS data store
    val glofas = CopernicusRequest(
        dataset = "cems-glofas-historical",
        inputs = mapOf(
            "variable" to listOf("river_discharge_in_the_last_24_hours"),
            "hyear" to listOf("2024"),
            "hmonth" to listOf("06"),
            "hday" to listOf("10"),
            "data_format" to "netcdf",
        ),
    )

    "toFileName is deterministic: the same request yields the same name" {
        glofas.toFileName() shouldBe glofas.toFileName()
    }

    "toFileName is stable under key reordering (the reason CanonicalJson exists)" {
        // same logical request, just reordered
        val reordered = CopernicusRequest(
            dataset = "cems-glofas-historical",
            inputs = mapOf(
                "data_format" to "netcdf",
                "hday" to listOf("10"),
                "hmonth" to listOf("06"),
                "hyear" to listOf("2024"),
                "variable" to listOf("river_discharge_in_the_last_24_hours"),
            ),
        )
        reordered.toFileName() shouldBe glofas.toFileName()
    }

    "a different dataset yields a different name" {
        glofas.copy(dataset = "reanalysis-era5-single-levels").toFileName() shouldNotBe glofas.toFileName()
    }

    "a different input value yields a different name (the bytes change)" {
        val otherDay = glofas.copy(inputs = glofas.inputs + ("hday" to listOf("11")))
        otherDay.toFileName() shouldNotBe glofas.toFileName()
    }

    "reordering a list inside inputs yields a different name (list order is semantic)" {
        // area corners reordered = a different request, must not collide
        val area = listOf(50.0, 5.0, 45.0, 10.0)
        val nwse = glofas.copy(inputs = glofas.inputs + ("area" to area))
        val swapped = glofas.copy(inputs = glofas.inputs + ("area" to area.reversed()))
        nwse.toFileName() shouldNotBe swapped.toFileName()
    }

    "the readable prefix comes from the dataset, sanitized" {
        glofas.toFileName() shouldStartWith "cems-glofas-historical_"
    }

    "a dataset id with unsafe characters is sanitized in the prefix" {
        val weird = glofas.copy(dataset = "weird/name with:chars")
        weird.toFileName() shouldStartWith "weird_name_with_chars_"
    }

    "the name complies with the CacheKey contract: a single file-system-safe segment" {
        glofas.toFileName() shouldMatch Regex("^[A-Za-z0-9._-]+$")
    }

    "the name is a readable prefix followed by a lowercase-hex hash suffix" {
        glofas.toFileName() shouldMatch Regex("^cems-glofas-historical_[0-9a-f]+$")
    }

    // toFileSystemSafe extension function tests
    "a plain name with only safe characters is left unchanged" {
        "cems-glofas-historical".toFileSystemSafe() shouldBe "cems-glofas-historical"
        "ERA5_2024.v3".toFileSystemSafe() shouldBe "ERA5_2024.v3"
    }

    "spaces are replaced" {
        "New York".toFileSystemSafe() shouldBe "New_York"
    }

    "path separators are replaced" {
        "a/b".toFileSystemSafe() shouldBe "a_b"
        "a\\b".toFileSystemSafe() shouldBe "a_b"
    }

    "Windows-illegal characters are replaced" {
        // < > : " | ? * are illegal in a Windows filename
        """a:b<c>d"e|f?g*h""".toFileSystemSafe() shouldBe "a_b_c_d_e_f_g_h"
    }

    "non-ASCII letters are replaced" {
        "Forlì-Cesena".toFileSystemSafe() shouldBe "Forl_-Cesena"
    }

    "output never contains a path separator, for any input" {
        listOf("a/b", "a\\b", "/", "\\", "C:/x", "../../etc").forEach { raw ->
            raw.toFileSystemSafe().shouldNotContain("/")
            raw.toFileSystemSafe().shouldNotContain("\\")
        }
    }

    "output contains only allowlisted characters" {
        """!"£$%&/()=?^@#°""".toFileSystemSafe() shouldMatch Regex("^[A-Za-z0-9._-]*$")
    }

    "sanitizing a sanitized name changes nothing" {
        val once = "a b/c:d".toFileSystemSafe()
        once.toFileSystemSafe() shouldBe once
    }
})
