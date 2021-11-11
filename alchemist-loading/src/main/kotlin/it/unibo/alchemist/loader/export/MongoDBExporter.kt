/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

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
 * @param appendTime if true it will always generate a new Mongo document, false to overwrite.
 */

class MongoDBExporter<T, P : Position<P>> @JvmOverloads constructor(
    private val uri: String,
    val dbname: String = DEFAULT_DATABASE,
    val interval: Double = DEFAULT_INTERVAL,
    private val appendTime: Boolean = false
) : AbstractExporter<T, P>(interval) {

    companion object {
        /**
         *  The default name used if no database name is specified.
         */
        private const val DEFAULT_DATABASE = "test"
    }

    override val exportDestination: String = uri
    /**
     * The name of the collection related to the current simulation in execution.
     */
    val collectionName: String
        get() = variablesDescriptor + "${if (appendTime) System.currentTimeMillis() else ""}"
    private val mongoService: MongoService = MongoService()

    override fun setupExportEnvironment(environment: Environment<T, P>) {
        mongoService.startService(uri)
        mongoService.connectToDB(dbname)
        mongoService.createCollection(collectionName)
    }

    override fun exportData(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        val document: Document = convertToDocument(environment, reaction, time, step)
        mongoService.pushToDatabase(document)
    }

    override fun closeExportEnvironment(environment: Environment<T, P>, time: Time, step: Long) {
        mongoService.stopService()
    }

    private fun convertToDocument(env: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long): Document {
        val document = Document()
        dataExtractors.stream()
            .forEach {
                it.extractData(env, reaction, time, step).forEach {
                    value ->
                    document.append(it.names.toString(), value)
                }
            }
        return document
    }
}
