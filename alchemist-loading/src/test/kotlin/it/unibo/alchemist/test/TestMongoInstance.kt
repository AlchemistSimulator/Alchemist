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
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import org.bson.Document

class TestMongoInstance :
    StringSpec({
        "test the local instance of mongo" {
            withMongo {
                val mongoClient: MongoClient = MongoClients.create()
                mongoClient shouldNotBe null
                val defaultDatabase: MongoDatabase = mongoClient.getDatabase("test")
                defaultDatabase shouldNotBe null
                val mongoCollection = mongoClient.getDatabase("test").getCollection("mongo-test-collection")
                mongoCollection.insertOne(Document("name", "mongo-test-document"))
                mongoCollection.countDocuments() shouldBeGreaterThan 0
                mongoCollection.find(eq("name", "mongo-test-document")).count() shouldBeGreaterThan 0
                mongoCollection.drop()
            }
        }
    })
