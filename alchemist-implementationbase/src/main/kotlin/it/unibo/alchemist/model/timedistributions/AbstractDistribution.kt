/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution

/**
 * Base class for time generators with an initial scheduling time.
 *
 * Neither value is part of the [TimeDistribution] contract: reactions may use this configuration when they are
 * constructed, but the distribution does not own occurrence state or lifecycle transitions.
 *
 * @property startTime earliest absolute time at which a reaction using this generator may run
 */
abstract class AbstractDistribution<T>(val startTime: Time) : TimeDistribution<T>
