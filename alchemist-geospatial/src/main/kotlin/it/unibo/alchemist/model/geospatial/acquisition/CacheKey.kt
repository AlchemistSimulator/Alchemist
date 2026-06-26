/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

/**
 * Deterministic identity of a request, used by [CacheManager] as the directory name for its
 * cache entry. Implemented by request types (e.g. [CopernicusRequest], [BBBikeRequest]).
 *
 * [CacheManager] depends on this interface and nothing else, so it stays unaware
 * of HTTP and of the individual provider APIs.
 */
interface CacheKey {

    /**
     * @return a **deterministic, file-system-safe** directory name derived *only* from what
     * determines the downloaded bytes, so that equal content always maps to the same name. The
     * returned name must be a single path segment: no separators (`/`, `\`), and safe on Linux,
     * macOS, and Windows.
     */
    fun toFileName(): String
}
