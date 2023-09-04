/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.model

import it.unibo.alchemist.core.BatchEngineConfiguration

/**
 * Contains the fixed size batch engine configuration parameters.
 * @property batchSize size of batch to be processed in parallel
 */
class FixedBatchEngineConfiguration(
    override val outputReplayStrategy: String,
    val batchSize: Int,
) : BatchEngineConfiguration

/**
 * Contains the epsilon dynamic batch engine configuration parameters.
 * @property epsilonValue epsilon sensitivity value used to build a batch
 * to be processed in parallel
 */
class EpsilonBatchEngineConfiguration(
    override val outputReplayStrategy: String,
    val epsilonValue: Double,
) : BatchEngineConfiguration
