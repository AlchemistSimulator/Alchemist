/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Mode

/**
 * Collection of benchmarks to be used with JMH
 */
@Suppress("unused")
open class Benchmarks {

    /**
     * Simple single-threaded simulation run
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 1)
    @Suppress("unused")
    fun singleThreadedSimulation() {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-p", "1", "-t", "100"))
    }
}
