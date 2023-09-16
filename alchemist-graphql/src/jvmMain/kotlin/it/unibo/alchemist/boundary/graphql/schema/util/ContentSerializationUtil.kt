/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.util

import it.unibo.alchemist.model.Concentration
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Utility function that converts a [Concentration] content of type [T] to a Json String.
 * If the serialization fails (i.e. the content does not provide a [serializer]), the string
 * representation of the content is returned.
 *
 * @param concentration The concentration containing the content to convert.
 * @return The Json String representation of the concentration content or its string representation.
 */
fun <T : Any> encodeConcentrationContentToString(concentration: Concentration<T>): String {
    val content: T = concentration.content
    return encodeConcentrationContentToString(content)
}

/**
 * Utility function that converts a [Concentration] content of type [T] to a Json String.
 * If the serialization fails (i.e. the content does not provide a [serializer]), the string
 * representation of the content is returned.
 *
 * @param content The concentration content to convert.
 * @return The Json String representation of the concentration content or its string representation.
 */
fun <T : Any> encodeConcentrationContentToString(content: T?): String {
    if (content == null) {
        return ""
    }
    return runCatching {
        Json.Default.encodeToString(serializer(content::class.java), content)
    }.getOrElse {
        content.toString()
    }
}
