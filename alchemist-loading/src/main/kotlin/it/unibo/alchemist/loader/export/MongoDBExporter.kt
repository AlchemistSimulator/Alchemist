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
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.bson.Document

/**
 * Exports data provided by a list of [Extractor]s on a MongoDB instance.
 * @param uri the connection URI of the database instance.
 * @param dbname the name the database to export data to.
 * @param interval the sampling time, defaults to [AbstractExporter.DEFAULT_INTERVAL].
 */

class MongoDBExporter<T, P : Position<P>> @JvmOverloads constructor(
    val uri: String,
    val dbname: String = DEFAULT_DATABASE,
    val interval: Double = DEFAULT_INTERVAL
) : AbstractExporter<T, P>(interval) {

    companion object {
        /**
         *  The default name used if no database name parameter is specified.
         */
        const val DEFAULT_DATABASE = "test"
    }

    private lateinit var database: MongoDatabase
    private lateinit var collection: MongoCollection<Document>

    override fun setupExportEnvironment(environment: Environment<T, P>) {
        val mongoClient: MongoClient = MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(uri))
                .build()
        )
        database = mongoClient.getDatabase(dbname)
        val collectionName: String = variablesDescriptor + System.currentTimeMillis()
        database.createCollection(collectionName)
        collection = database.getCollection(collectionName)
    }

    override fun exportData(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        val document = Document()
        dataExtractor.stream()
            .forEach {
                it.extractData(environment, reaction, time, step).forEach {
                        value -> document.append(it.names.toString(), value)
                }
            }
        collection.insertOne(document)
    }

    override fun closeExportEnvironment(environment: Environment<T, P>, time: Time, step: Long) {
        // TODO
    }
}
