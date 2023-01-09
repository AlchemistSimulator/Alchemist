/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import java.io.File

/**
 * Used to create a pseudo-randomly named temporary file for testing purposes.
 */
object TemporaryFile {

    /**
     * Creates a temporary file named after the method that calls the function, for testing purposes only.
     */
    @JvmStatic
    fun create(): File = File.createTempFile(
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { stack ->
            stack.skip(1)
                .map(StackWalker.StackFrame::getMethodName)
                .findFirst()
                .orElse("unknownMethod")
        },
        null
    )
}
