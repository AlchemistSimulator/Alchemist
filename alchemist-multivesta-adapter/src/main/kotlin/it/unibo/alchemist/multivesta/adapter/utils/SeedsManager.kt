/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter.utils

import java.io.File
import kotlin.random.Random

/**
 * This object is used to manage the list of available seeds while running analysis
 * on an already executed Alchemist simulation. It is used to avoid running the same
 * simulation twice.
 */
object SeedsManager {
    private const val folder = "tmp"
    private val availableSeedsPath = File("$folder/available_seeds.txt")

    private val lock: FileLock = FileLock("seeds")

    /**
     * Creates the file containing the list of available seeds.
     * @param seeds the list of available seeds
     */
    fun createAvailableSeedsFile(seeds: Collection<Int>) {
        availableSeedsPath.writeText(seeds.joinToString(" "))
    }

    /**
     * Returns a random seed from the list of available seeds and removes it from the list.
     * @param seed the seed used to randomize the selection
     * @return a random seed from the list of available seeds
     */
    fun popNextAvailableSeed(seed: Int): Int? {
        return lock.doWithLock {
            val availableSeeds = availableSeedsPath.readText().split(" ").map { it.toIntOrNull() }.toMutableList()
            availableSeeds.randomOrNull(Random(seed))?.also {
                availableSeeds.remove(it)
                availableSeedsPath.writeText(availableSeeds.joinToString(" "))
            }
        }
    }
}
