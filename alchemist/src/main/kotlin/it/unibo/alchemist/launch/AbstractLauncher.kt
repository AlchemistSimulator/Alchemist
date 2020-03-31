/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.launch

/**
 * Provides utility functions for [Launcher] implementors.
 */
abstract class AbstractLauncher : Launcher {

    /**
     * Creates a [Validation.Invalid] expliciting what is the option this [Launcher] is incompatible with.
     */
    protected fun incompatibleWith(option: String) =
        Validation.Invalid("$name is not compatible with $option")

    /**
     * Creates a [Validation.Invalid] expliciting which option is required to run this [Launcher].
     */
    protected fun requires(option: String) =
        Validation.Invalid("$name requires $option")
}
