/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.modules

import io.ktor.server.application.Application
import java.net.InetAddress

/**
 * Start the default browser of the user on the server address.
 */
fun Application.startBrowserModule() {
    Runtime.getRuntime().exec("xdg-open ${InetAddress.getLocalHost().hostAddress}:$port")
}

/**
 * Retrieve the port used in the Ktor application.conf file.
 */
val Application.port get() = environment.config.property("ktor.deployment.port").getString().toInt()
