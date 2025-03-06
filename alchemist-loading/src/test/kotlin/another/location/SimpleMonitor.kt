/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package another.location

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time

class SimpleMonitor<T, P> : OutputMonitor<T, P> where P : Position<P> {
    var initialized: Boolean = false
    var finished: Boolean = false

    override fun initialized(environment: Environment<T, P>) {
        super.initialized(environment)
        initialized = true
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        super.finished(environment, time, step)
        finished = true
    }
}
