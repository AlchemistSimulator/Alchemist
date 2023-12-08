/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.exporters

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

/**
 * Contains all the functions in order to use MongoDB Database.
 */
class MongoService {

    private lateinit var client: MongoClient
    private lateinit var database: MongoDatabase
    private lateinit var collection: MongoCollection<Document>

    /**
     *  Requires an active instance of MongoDB at the given uri.
     */
    fun startService(uri: String) {
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .build()
        client = MongoClients.create(settings)
    }

    /**
     * Connects to a specific database.
     * If there is no database with the input name in the mongo instance, an empty one will be created.
     */
    fun connectToDB(dbName: String) {
        database = client.getDatabase(dbName)
    }

    /**
     * Creates a collection inside the Mongo database.
     * If there is already a collection with the parameter name,
     */
    fun createCollection(collectionName: String) {
        collection = database.getCollection(collectionName)
    }

    /**
     * Send the created document to the Mongo collection.
     */
    fun pushToDatabase(document: Document) {
        collection.insertOne(document)
    }

    /**
     * Close the connection with the Mongo instance.
     */
    fun stopService() {
        client.close()
    }
}
