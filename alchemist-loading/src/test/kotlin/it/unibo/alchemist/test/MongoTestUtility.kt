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
import de.flapdoodle.embed.process.runtime.Network
import io.kotest.matchers.shouldNotBe
import net.harawata.appdirs.AppDirsFactory
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

private object GlobalLock
private val starter: MongodStarter = MongodStarter.getDefaultInstance()
private val sharedDirectory = AppDirsFactory.getInstance().getUserCacheDir("alchemist", "test", "alchemist")
    .also { File(it).mkdirs() }
private val lockFileLocation = File(sharedDirectory, "mongoDbTestLock.lock").also { it.createNewFile() }

internal fun withMongo(operation: () -> Unit) {
    synchronized(GlobalLock) {
        FileChannel.open(Path(lockFileLocation.absolutePath), StandardOpenOption.WRITE).lock().use {
            val mongodConfig: ImmutableMongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(Net("localhost", 27017, Network.localhostIsIPv6()))
                .build()
            val mongodExecutable: MongodExecutable = starter.prepare(mongodConfig)
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
}
