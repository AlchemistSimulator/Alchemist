/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.state

import org.reduxkotlin.Store
import org.reduxkotlin.createThreadSafeStore

/**
 * The store of the server. Can be accesed anywhere thanks to the singleton pattern.
 */
object ServerStore {
    /**
     * The redux store of the server.
     */
    val store: Store<ServerState> = createThreadSafeStore(::rootReducer, ServerState())
}
