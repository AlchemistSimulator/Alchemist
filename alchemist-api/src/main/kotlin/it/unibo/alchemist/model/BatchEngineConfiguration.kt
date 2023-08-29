/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * Contains the batch engine configuration parameters.
 *
 *  @property outputReplayStrategy events output replay strategy for parallel engines, defaults to aggregate
 *  @property workersNumber number of used workers for parallel engines, defaults to cores available
 */
interface BatchEngineConfiguration : EngineConfiguration {

    val outputReplayStrategy: String
}
