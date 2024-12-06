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
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.boundary.exporters.MongoDBExporter
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader

class TestMongoExporter<T, P : Position<P>> :
    StringSpec({
        "test exporting data on MongoDB" {
            withMongo {
                val file = ResourceLoader.getResource("testMongoExporter.yml")
                assertNotNull(file)
                val loader = LoadAlchemist.from(file)
                assertNotNull(loader)
                val simulation: Simulation<T, P> = loader.getDefault()

                fun checkForErrors() = simulation.error.ifPresent { throw it }
                simulation.addOutputMonitor(
                    object : OutputMonitor<T, P> {
                        override fun finished(
                            environment: Environment<T, P>,
                            time: Time,
                            step: Long,
                        ) = checkForErrors()

                        override fun initialized(environment: Environment<T, P>) = checkForErrors()

                        override fun stepDone(
                            environment: Environment<T, P>,
                            reaction: Actionable<T>?,
                            time: Time,
                            step: Long,
                        ) = checkForErrors()
                    },
                )
                simulation.play()
                simulation.run()
                checkForErrors()
                val exporter =
                    simulation.outputMonitors
                        .filterIsInstance<GlobalExporter<T, P>>()
                        .flatMap { it.exporters }
                        .apply { size shouldBeExactly 1 }
                        .firstOrNull { it is MongoDBExporter }
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
