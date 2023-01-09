/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import org.gradle.api.Project
import kotlin.String

/**
 * Statically defined libraries used by the project.
 */
@Suppress("UndocumentedPublicProperty")
object Libs {

    /**
     * Returns a reference to an alchemist sub-project [module].
     */
    fun Project.alchemist(module: String): Project = project(":alchemist-$module")

    /**
     * Returns a reference to an alchemist sub-project incarnation [module].
     */
    fun Project.incarnation(module: String): Project = alchemist("incarnation-$module")
}
