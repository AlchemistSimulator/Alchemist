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
 * Collection of benchmarks to be used with JMH.
 */
@Suppress("unused", "MagicNumber")
open class Benchmarks {

    /**
     * Single-threaded and deterministic simulation run.
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(1)
    @Suppress("unused")
    fun singleThreadedSimulation() {
        Alchemist.main(
            arrayOf(
                "run",
                "simulation.yml",
                "--verbosity",
                "warn",
                "--override",
                """
                launcher:
                    parameters:
                        parallelism: 1
                """.trimIndent(),
            ),
        )
    }

    /**
     * Multithreaded simulation run with batch engine
     * in fixed-size batch mode that uses 4 threads, this benchmark should
     * perform well on most modern processors.
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(4)
    @Suppress("unused")
    fun multiThreadedSimulationFourThreadsFixedBatch() {
        Alchemist.main(
            arrayOf(
                "run",
                "simulation.yml",
                "--verbosity",
                "warn",
                "--override",
                """
                launcher:
                    parameters:
                        parallelism: 4
                engine-configuration:
                    type: FixedBatchEngineConfiguration
                    parameters:
                        outputReplayStrategy: aggregate
                        workersNumber: 4
                        batchSize: 4
                """.trimIndent(),
            ),
        )
    }

    /**
     * Multithreaded simulation run with batch engine
     * in fixed-size batch mode that uses 8 threads, this benchmark should perform
     * well on higher-end processors.
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(8)
    @Suppress("unused")
    fun multiThreadedSimulationEightThreadsFixedBatch() {
        Alchemist.main(
            arrayOf(
                "run",
                "simulation.yml",
                "--verbosity",
                "warn",
                "--override",
                """
                launcher:
                    parameters:
                        parallelism: 8
                engine-configuration:
                    type: FixedBatchEngineConfiguration
                    parameters:
                        outputReplayStrategy: aggregate
                        workersNumber: 4
                        batchSize: 4
                """.trimIndent(),
            ),
        )
    }

    /**
     * Multithreaded simulation run with batch engine
     * in epsilon dynamic batch mode that uses 4 threads, this benchmark should
     * perform well on most modern processors.
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(4)
    @Suppress("unused")
    fun multiThreadedSimulationFourThreadsEpsilonBatch() {
        Alchemist.main(
            arrayOf(
                "run",
                "simulation.yml",
                "--verbosity",
                "warn",
                "--override",
                """
                launcher:
                    parameters:
                        parallelism: 4
                engine-configuration:
                    type: EpsilonBatchEngineConfiguration
                    parameters:
                        outputReplayStrategy: aggregate
                        workersNumber: 4
                        epsilonValue: 0.01
                """.trimIndent(),
            ),
        )
    }

    /**
     * Multithreaded simulation run with batch engine
     * epsilon dynamic batch mode that uses 8 threads, this benchmark should perform
     * well on higher-end processors.
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3)
    @Threads(8)
    @Suppress("unused")
    fun multiThreadedSimulationEightThreadsEpsilonBatch() {
        Alchemist.main(
            arrayOf(
                "run",
                "simulation.yml",
                "--verbosity",
                "warn",
                "--override",
                """
                launcher:
                    parameters:
                        parallelism: 8
                engine-configuration:
                    type: EpsilonBatchEngineConfiguration
                    parameters:
                        outputReplayStrategy: aggregate
                        workersNumber: 8
                        epsilonValue: 0.01
                """.trimIndent(),
            ),
        )
    }
}
