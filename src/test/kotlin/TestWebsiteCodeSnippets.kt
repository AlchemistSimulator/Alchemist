import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.LoadAlchemist

/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestWebsiteCodeSnippets : FreeSpec(
    {
        "all snippets should load correctly" {
            ClassPathScanner.resourcesMatching(".*", "website-snippets")
                .also { it shouldNot beEmpty() }
                .onEach { it shouldNot beNull() }
                .forEach { LoadAlchemist.from(it).getDefault<Any, Nothing>().environment shouldNot beNull() }
        }
    }
)
