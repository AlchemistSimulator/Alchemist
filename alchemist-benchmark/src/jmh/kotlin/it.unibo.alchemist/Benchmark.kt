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

open class Benchmark {

    @Benchmark
    @BenchmarkMode(Mode.All)
    @Fork(value = 3, warmups = 3)
    fun test1() {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-t", "50"))
    }
}
