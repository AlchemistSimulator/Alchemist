/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.build

import java.io.File
import org.apache.tools.ant.taskdefs.condition.Os

val isInCI get() = System.getenv("CI") == true.toString()
val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
val isMac = Os.isFamily(Os.FAMILY_MAC)
val isUnix = Os.isFamily(Os.FAMILY_UNIX)

fun commandExists(command: String) = System.getenv("PATH").split(File.pathSeparatorChar)
    .any { path -> File(path, command).run { exists() && canExecute() } }
