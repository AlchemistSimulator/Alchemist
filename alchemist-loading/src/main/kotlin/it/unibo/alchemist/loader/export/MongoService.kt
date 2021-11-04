/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

class MongoService {

    private lateinit var client: MongoClient
    private lateinit var settings: MongoClientSettings
    private lateinit var database: MongoDatabase
    private lateinit var collection: MongoCollection<Document>


    fun startService(uri: String) {
        settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .build()
        client = MongoClients.create(settings)
    }

    fun connectToDB(dbName: String) {
        database = client.getDatabase(dbName)
    }

    fun createCollection(collectionName: String) {
        database.createCollection(collectionName)
        collection = database.getCollection(collectionName)
    }

    fun pushToDatabase(document: Document) {
        collection.insertOne(document)
    }
}
