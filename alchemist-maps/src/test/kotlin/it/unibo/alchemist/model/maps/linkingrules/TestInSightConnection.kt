/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.linkingrules

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.maps.environments.OSMEnvironment
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
            val rule = environment.linkingRule
            rule shouldBe instanceOf<ConnectIfInLineOfSigthOnMap<*>>()
            val maxRange = (rule as ConnectIfInLineOfSigthOnMap<*>).maxRange
            environment.getDistanceBetweenNodes(node0, node1) shouldBeLessThan maxRange
            val route = environment.computeRoute(node0, node1)
            route.length() shouldBeGreaterThan maxRange
            environment.getNeighborhood(node0).contains(node1).shouldBeFalse()
        }
    },
)
