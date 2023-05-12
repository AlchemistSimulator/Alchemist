/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import java.io.Serializable

/**
 * Tag interface, used to track the set of options available for a [RoutingService].
 */
interface RoutingServiceOptions<out O : RoutingServiceOptions<O>> : Serializable
