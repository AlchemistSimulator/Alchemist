import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.LoadAlchemist
import java.net.URL

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
        val allSpecs = ClassPathScanner.resourcesMatching(".*", "website-snippets")
            .also { it shouldNot beEmpty() }
            .onEach { it shouldNot beNull() }
        "all snippets should load correctly and run for a bit" {
            allSpecs.load().forEach { env -> Engine(env, 1000L).also { it.play() }.run() }
        }
        "snippets with displacements should have nodes" {
            allSpecs.asSequence()
                .filter { it.readText().contains("^displacements:") }
                .load()
                .forEach { it.nodes shouldNot beEmpty() }
        }
    }
) {
    companion object {
        fun List<URL>.load() = asSequence().load()
        fun Sequence<URL>.load() = map { LoadAlchemist.from(it).getDefault<Any, Nothing>().environment }
    }
}
