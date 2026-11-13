/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable

/**
 * The world-space bounds of the viewport scene.
 *
 * @property minX the smallest x.
 * @property maxX the largest x.
 * @property minY the smallest y.
 * @property maxY the largest y.
 */
@Immutable
internal data class ViewportWorldBounds(val minX: Double, val maxX: Double, val minY: Double, val maxY: Double)
