/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.slf4j.LoggerFactory

/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class LoggingEnvironment(
    incarnation: Incarnation<Any, Euclidean2DPosition>,
) : Continuous2DEnvironment<Any>(incarnation) {

    init {
        val logger = LoggerFactory.getLogger(LoggingEnvironment::class.java)
        logger.trace("TRACE MESSAGE")
        logger.debug("DEBUG MESSAGE")
        logger.info("INFO MESSAGE")
        logger.warn("WARN MESSAGE")
        logger.error("ERROR MESSAGE")
    }
}
