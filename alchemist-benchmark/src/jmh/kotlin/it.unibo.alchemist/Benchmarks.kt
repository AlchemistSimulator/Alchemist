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
import org.openjdk.jmh.annotations.Threads

/**
 * Collection of benchmarks to be used with JMH
 */
@Suppress("unused")
open class Benchmarks {

    /**
     * Singlethreaded and deterministic simulation run
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(1)
    @Suppress("unused")
    fun singleThreadedSimulation() {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-p", "1", "-t", "50"))
    }

    /**
     * Multithreaded simulation run with experimental batch engine enabled that uses 4 threads, this benchmark should
     * perform well on most modern processors
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(5)
    @Suppress("unused")
    fun multiThreadedSimulationFourCoresCores() {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-f", "batch", "-p", "4", "-t", "50"))
    }

    /**
     * Multithreaded simulation run with experimental batch engine enabled that uses 8 threads, this benchmark should perform
     * well on higher-end processors
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(9)
    @Suppress("unused")
    fun multiThreadedSimulationEightCoresCores() {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-f", "batch", "-p", "8", "-t", "50"))
    }
}
