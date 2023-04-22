/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test
import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ClosestN
import it.unibo.alchemist.model.implementations.nodes.GenericNode

class TestClosestNOnMaps : StringSpec({
    "Use ClosestN on maps" {
        val environment = OSMEnvironment(
            SupportedIncarnations.get<Any, GeoPosition>("protelis").orElseGet { TODO() },
            "maps/cesena.pbf",
        )
        environment.linkingRule = ClosestN(10)
        environment.addNode(
            object : GenericNode<Any>(environment) {
                override fun createT() = "Nothing"
            },
            environment.makePosition(44.139169, 12.237816),
        )
    }
})
