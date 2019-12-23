/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import java.io.File

object TemporaryFile {
    @JvmStatic
    fun create() = File.createTempFile(
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { stack ->
            stack.skip(1)
                .map(StackWalker.StackFrame::getMethodName)
                .findFirst()
                .orElse("unknownMethod")
        },
        null
    )
}
