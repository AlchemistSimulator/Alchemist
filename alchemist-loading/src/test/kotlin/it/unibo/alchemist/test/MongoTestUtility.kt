/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.os.ImmutablePlatform
import de.flapdoodle.os.OS
import de.flapdoodle.os.Platform
import de.flapdoodle.os.linux.LinuxDistribution
import de.flapdoodle.os.linux.UbuntuVersion
import de.flapdoodle.reverse.StateID
import de.flapdoodle.reverse.transitions.Start
import org.slf4j.LoggerFactory

private val logger by lazy { LoggerFactory.getLogger("TestsWithMongoDB") }

private inline fun <reified T : Any> T.toTransition() = Start.to(StateID.of(T::class.java)).initializedWith(this)

internal fun withMongo(operation: () -> Unit) {
    if (System.getProperty("os.name").lowercase().contains("win")) {
        logger.warn("Testing with MongoDB is disabled on Windows due to flaky behavior")
    } else {
        val detectedPlatform = Platform.detect()
        val platform: Platform = detectedPlatform
            .takeUnless { it.operatingSystem() == OS.Linux && it.distribution().isEmpty }
            ?: ImmutablePlatform.builder()
                .from(detectedPlatform)
                .distribution(LinuxDistribution.Ubuntu)
                .version(UbuntuVersion.Ubuntu_20_10)
                .build()
        val mongod = Mongod.instance()
            .withNet(Net.of("localhost", 27017, Network.localhostIsIPv6()).toTransition())
            .withPlatform(platform.toTransition())
        val runningState = mongod.start(Version.Main.V6_0)
        try {
            operation()
        } finally {
            runningState.current().stop()
        }
    }
}
