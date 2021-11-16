/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import com.mongodb.MongoException
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq

import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.loader.export.exporters.MongoDBExporter

import org.bson.Document
import org.junit.platform.commons.logging.LoggerFactory

class TestMongoInstance : StringSpec({
    "test the local instance of mongo" {

        val starter: MongodStarter = MongodStarter.getDefaultInstance()
        val mongodConfig: ImmutableMongodConfig = MongodConfig.builder()
            .version(Version.Main.PRODUCTION)
            .net(Net("localhost", 27017, Network.localhostIsIPv6()))
            .build()
        val mongodExecutable: MongodExecutable = starter.prepare(mongodConfig)
        mongodExecutable shouldNotBe null
        val mongodProcess: MongodProcess = mongodExecutable.start()
        mongodProcess shouldNotBe null

        try {
            val mongoClient: MongoClient = MongoClients.create()
            mongoClient shouldNotBe null
            val defaultDatabase: MongoDatabase = mongoClient.getDatabase("test")
            defaultDatabase shouldNotBe null
            val mongoCollection = mongoClient.getDatabase("test").getCollection("mongo-test-collection")
            mongoCollection.insertOne(Document("name", "mongo-test-document"))
            mongoCollection.countDocuments() shouldBeGreaterThan 0
            mongoCollection.find(eq("name", "mongo-test-document")).count() shouldBe 1
        } catch (exception: MongoException) {
            LoggerFactory.getLogger(MongoDBExporter::class.java).error(exception) {
                "Can't start a local mongo instance for tests."
            }
        } finally {
            mongodProcess.stop()
            mongodExecutable.stop()
        }
    }
})
