/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.exporters.GlobalExporter
import it.unibo.alchemist.loader.export.exporters.MongoDBExporter
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Actionable
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Time
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
            simulation.addOutputMonitor(GlobalExporter(initialized.exporters))
            fun checkForErrors() = simulation.error.ifPresent { throw it }
            simulation.addOutputMonitor(object : OutputMonitor<T, P> {
                override fun finished(environment: Environment<T, P>, time: Time, step: Long) = checkForErrors()
                override fun initialized(environment: Environment<T, P>) = checkForErrors()
                override fun stepDone(
                    environment: Environment<T, P>,
                    reaction: Actionable<T>?,
                    time: Time,
                    step: Long
                ) = checkForErrors()
            })
            simulation.play()
            simulation.run()
            checkForErrors()
            val exporter = initialized.exporters.firstOrNull {
                it is MongoDBExporter
            }
            require(exporter is MongoDBExporter)
            exporter.dataExtractors.size shouldBeGreaterThan 0
            val testClient: MongoClient = MongoClients.create(exporter.uri)
            val exportCollection = testClient.getDatabase(exporter.dbName).getCollection(exporter.collectionName)
            exportCollection.countDocuments() shouldBeGreaterThan 0
            val columns = exporter.dataExtractors.first().columnNames[0]
            exportCollection.find().forEach { document ->
                document.keys.shouldContainAll(columns)
            }
        }
    }
})
