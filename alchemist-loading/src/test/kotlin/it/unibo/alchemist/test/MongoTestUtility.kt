/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.distribution.Distribution
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.os.ImmutablePlatform
import de.flapdoodle.os.OS
import de.flapdoodle.os.linux.LinuxDistribution
import de.flapdoodle.os.linux.UbuntuVersion
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory

private val starter: MongodStarter = MongodStarter.getDefaultInstance()
private val logger by lazy { LoggerFactory.getLogger("TestsWithMongoDB") }

internal fun withMongo(operation: () -> Unit) {
    if (System.getProperty("os.name").lowercase().contains("win")) {
        logger.warn("Testing with MongoDB is disabled on Windows due to flaky behavior")
    } else {
        val mongodConfig: ImmutableMongodConfig = MongodConfig.builder()
            .version(Version.Main.V4_4)
            .net(Net("localhost", 27017, Network.localhostIsIPv6()))
            .build()
        val detectedDistribution = Distribution.detectFor(mongodConfig.version())
        val actualDistribution = detectedDistribution
            .takeUnless { with(it.platform()) { operatingSystem() == OS.Linux && distribution().isEmpty } }
            ?: Distribution.of(
                mongodConfig.version(),
                ImmutablePlatform.builder()
                    .from(detectedDistribution.platform())
                    .distribution(LinuxDistribution.Ubuntu)
                    .version(UbuntuVersion.Ubuntu_20_10)
                    .build()
            )
        val mongodExecutable: MongodExecutable = starter.prepare(mongodConfig, actualDistribution)
        mongodExecutable shouldNotBe null
        val mongodProcess: MongodProcess = mongodExecutable.start()
        mongodProcess shouldNotBe null
        try {
            operation()
        } finally {
            mongodExecutable.stop()
        }
    }
}
