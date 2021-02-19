/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class ConstructorOrString private constructor(source: Either<String, JVMConstructor>) {
    val string: String? = source.leftOrNull
    val constructor: JVMConstructor? = source.rightOrNull

    companion object {
        @JsonCreator
        @JvmStatic
        fun buildFromString(stringDescriptor: String) = ConstructorOrString(Either.left(stringDescriptor))

        @JsonCreator @JvmStatic
        fun buildFromTypeAndParameters(type: String, parameters: Iterable<*>?)
            = ConstructorOrString(Either.right(JVMConstructor.createConstructor(type, parameters)))
    }

    override fun toString(): String = string ?: constructor?.toString() ?: TODO()
}
