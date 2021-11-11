/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import it.unibo.alchemist.model.interfaces.GeoPosition
import org.kaikikm.threadresloader.ResourceLoader

class TestInSightConnection : StringSpec(
    {
        "environments with in-sight link on maps should be loadable" {
            val environment = LoadAlchemist.from(ResourceLoader.getResource("simulations/connect-sight.yml"))
                .getDefault<Nothing, GeoPosition>()
                .environment as OSMEnvironment
            environment.nodeCount shouldBe 102
            val node0 = environment.getNodeByID(0)
            val node1 = environment.getNodeByID(1)
            environment.getDistanceBetweenNodes(node0, node1) shouldBeLessThan 100.0
            val route = environment.computeRoute(node0, node1)
            route.length() shouldBeGreaterThan 100.0
            environment.getNeighborhood(node0) shouldNotContain node1
        }
    }
)
