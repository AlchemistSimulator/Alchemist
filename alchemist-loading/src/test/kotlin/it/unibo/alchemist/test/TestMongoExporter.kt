/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldContainKey
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.exporters.GlobalExporter
import it.unibo.alchemist.loader.export.exporters.MongoDBExporter
import it.unibo.alchemist.model.interfaces.Position
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader

class TestMongoExporter<T, P : Position<P>> : StringSpec({
    "test exporting data on MongoDB" {
        withMongo {
            val file = ResourceLoader.getResource("testMongoExporter.yml")
            assertNotNull(file)
            val loader = LoadAlchemist.from(file)
            assertNotNull(loader)
            val initialized: InitializedEnvironment<T, P> = loader.getDefault()
            val simulation = Engine(initialized.environment)
            initialized.exporters.forEach {
                it.bindVariables(loader.variables)
            }
            simulation.addOutputMonitor(GlobalExporter(initialized.exporters))
            simulation.play()
            simulation.run()
            val exporter = initialized.exporters.firstOrNull {
                it is MongoDBExporter
            }
            require(exporter is MongoDBExporter) {
                exporter as MongoDBExporter
            }
            exporter.dataExtractors.size shouldBeGreaterThan 0
            val testClient: MongoClient = MongoClients.create(exporter.exportDestination)
            val exportCollection = testClient.getDatabase(exporter.dbName).getCollection(exporter.collectionName)
            exportCollection.countDocuments() shouldBeGreaterThan 0
            exportCollection.find().firstOrNull()?.shouldContainKey(
                exporter.dataExtractors.first().columnNames[0]
            )
        }
    }
}) {
    companion object {
        private fun withMongo(operation: () -> Unit) {
            val starter: MongodStarter = MongodStarter.getDefaultInstance()
            val mongodConfig: ImmutableMongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(Net("localhost", 27017, Network.localhostIsIPv6()))
                .build()
            val mongodExecutable: MongodExecutable = starter.prepare(mongodConfig)
            val mongodProcess: MongodProcess = mongodExecutable.start()
            try {
                operation()
            } finally {
                mongodProcess.stop()
                mongodExecutable.stop()
            }
        }
    }
}
