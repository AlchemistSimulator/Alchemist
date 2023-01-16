/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server

import io.kotest.core.spec.style.StringSpec
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import it.unibo.alchemist.server.modules.installModule
import it.unibo.alchemist.server.modules.routingModule
import it.unibo.alchemist.server.routes.mainRouteTest

/**
 * Test for Ktor Server.
 */
open class ServerTest : StringSpec({
    "Server Routes should behave correctly" {
        testApplication {
            application {
                installModule()
                routingModule()
            }
            environment {
                config = MapApplicationConfig(
                    "ktor.deployment.port" to "80",
                    "ktor.deployment.sslPort" to "7001"
                )
            }
            mainRouteTest()
        }
    }
})
