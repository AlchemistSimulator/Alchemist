/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.syntax

internal class DescriptorBuilder {
    private var forbiddenKeys = emptySet<String>()
    private var mandatoryKeys = emptySet<String>()
    private var optionalKeys = emptySet<String>()
    fun forbidden(vararg names: String) {
        forbiddenKeys += names.toSet()
    }
    fun mandatory(vararg names: String) {
        mandatoryKeys += names.toSet()
    }
    fun optional(vararg names: String) {
        optionalKeys += names.toSet()
    }
    fun build() = SyntaxElement.ValidDescriptor(mandatoryKeys, optionalKeys, forbiddenKeys)
}
