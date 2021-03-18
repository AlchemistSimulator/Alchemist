/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import it.unibo.alchemist.loader.LoadAlchemist
import org.kaikikm.threadresloader.ResourceLoader

class TestGraphStream : FreeSpec({
    "the lobster displacement should" - {
        val environment = LoadAlchemist.from(ResourceLoader.getResource("graphstream/testlobster.yml"))
            .getDefault<Nothing, Nothing>()
            .environment
        "displace all nodes" - {
            environment.nodeCount shouldBeExactly 400
            "with neighbors closer than non-neighbors" {
                environment.nodes.forEach { node ->
                    val neighborhood = environment.getNeighborhood(node)
                    val averageDistances = environment.nodes.asSequence()
                        .groupBy { it in neighborhood }
                        .mapValues { (_, nodes) ->
                            nodes.asSequence()
                                .map { environment.getDistanceBetweenNodes(node, it) }
                                .average()
                        }
                    2 * averageDistances[true]!! shouldBeLessThan averageDistances[false]!!
                }
            }
        }
        "create links" - {
            val neighborhoods = environment.nodes
                .map { environment.getNeighborhood(it).neighbors }
            neighborhoods.forEach { it.shouldNotBeEmpty() }
            "asymmetrically" {
                println(neighborhoods)
                neighborhoods.map { it.size }.toSet().size shouldBeGreaterThan 2
            }
        }
    }
})
