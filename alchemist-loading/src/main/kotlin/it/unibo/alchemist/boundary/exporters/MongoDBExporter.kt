/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.exporters

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import org.bson.Document

/**
 * Exports data to a MongoDB instance.
 *
 * @param uri the connection URI of the database instance.
 * @param dbName the name the database to export data to.
 * @param interval the sampling time, defaults to [AbstractExporter.DEFAULT_INTERVAL].
 * @param appendTime if true it will always generate a new Mongo document, false to overwrite.
 */
class MongoDBExporter<T, P : Position<P>> @JvmOverloads constructor(
    val uri: String,
    val dbName: String = DEFAULT_DATABASE,
    val interval: Double = DEFAULT_INTERVAL,
    private val appendTime: Boolean = false,
) : AbstractExporter<T, P>(interval) {

    /**
     * The name of the collection related to the current simulation in execution.
     */
    lateinit var collectionName: String
        private set

    private val mongoService: MongoService = MongoService()

    override fun setup(environment: Environment<T, P>) {
        collectionName = "$variablesDescriptor${"".takeUnless { appendTime } ?: System.currentTimeMillis()}"
        mongoService.startService(uri)
        mongoService.connectToDB(dbName)
        mongoService.createCollection(collectionName)
    }

    override fun exportData(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        mongoService.pushToDatabase(convertToDocument(environment, reaction, time, step))
    }

    override fun close(environment: Environment<T, P>, time: Time, step: Long) {
        mongoService.stopService()
    }

    private fun convertToDocument(
        environment: Environment<T, P>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Document {
        val document = Document()
        dataExtractors.forEach { extractor ->
            extractor.extractData(environment, reaction, time, step).forEach { (dataLabel, dataValue) ->
                document.append(dataLabel, dataValue)
            }
        }
        return document
    }

    companion object {
        /**
         *  The default database if no name is specified.
         */
        private const val DEFAULT_DATABASE = "test"
    }
}
