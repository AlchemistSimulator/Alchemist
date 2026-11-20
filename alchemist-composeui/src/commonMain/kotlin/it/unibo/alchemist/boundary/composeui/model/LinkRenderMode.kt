/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

/**
 * Effective link rendering strategy used for the current scene snapshot.
 */
internal enum class LinkRenderMode {
    /** All links are drawn. */
    FULL,

    /** Only a sample of the links is drawn. */
    SAMPLED,

    /** Too many links: none is drawn. */
    HIDDEN,
}
