/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import java.nio.file.Path
import kotlin.io.path.exists
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Resolves [path] on the file system if it exists there, or among the classpath resources otherwise,
 * as other Alchemist components do for their data files.
 *
 * @throws IllegalArgumentException if [path] is neither an existing path nor a classpath resource.
 */
internal fun pathOrClasspathResource(path: String): Path = Path.of(path).takeIf { it.exists() }
    ?: ResourceLoader.getResource(path)?.let { Path.of(it.toURI()) }
    ?: throw IllegalArgumentException("'$path' is neither an existing path nor a classpath resource")
