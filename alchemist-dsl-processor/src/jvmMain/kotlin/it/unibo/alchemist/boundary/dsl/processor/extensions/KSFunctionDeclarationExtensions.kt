/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * Finds a public constructor for the given class declaration.
 * Prefers the primary constructor, otherwise returns the constructor
 * with the most parameters.
 *
 * @return The found constructor, or null if no suitable constructor exists
 */
internal val KSFunctionDeclaration.parameterTypes: List<KSType> get() = parameters.map { it.type.resolve() }
