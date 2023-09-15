/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.operations

import com.expediagroup.graphql.server.operations.Query

/**
 * A GraphQL test query.
 * This is used to test the GraphQL module.
 * Once domain specific queries are implemented, this class will be removed.
 */
class TestQuery : Query {
    /**
     * A test query that return today date in ms.
     */
    fun today() = "Today is ${System.currentTimeMillis()}"
}
