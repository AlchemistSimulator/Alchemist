/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.yaml

import arrow.core.Either

/**
 * Returns the left of this [Either] if it is left, and null otherwise.
 */
val <A, B> Either<A, B>.leftOrNull get(): A? = if (this is Either.Left) a else null

/**
 * Returns the right of this [Either] if it is right, and null otherwise.
 */
val <A, B> Either<A, B>.rightOrNull get(): B? = if (this is Either.Right) b else null
