/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import another.location.MyTestEnv
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.types.instanceOf
import it.unibo.alchemist.testsupport.loadAlchemistFromResource

class TestLoadingFromCustomPackage : StringSpec({
    "classes in custom packages can be loaded" {
        val loader = loadAlchemistFromResource("regression/2023-lmcs-custom-package-loading.yml")
        loader shouldNot beNull()
        loader.getDefault<Nothing, Nothing>().environment shouldBe instanceOf(MyTestEnv::class)
    }
})
