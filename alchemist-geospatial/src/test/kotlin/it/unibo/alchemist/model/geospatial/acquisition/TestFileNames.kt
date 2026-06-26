/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotContain

class TestFileNames : StringSpec({

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
