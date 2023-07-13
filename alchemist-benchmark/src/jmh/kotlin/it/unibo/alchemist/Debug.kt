/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

object Debug {

    @JvmStatic
    fun main(args: Array<String>) {
        Alchemist.main(
            arrayOf(
                "run", "--verbosity", "info",
                "--override", "_test.str=test",
                "--override", "_test.int=10",
                "--override", "_test.dbl=10.1",
                "--override", "_test.strL=[test1, test2]",
                "--override", "_test.intL=[9, 19]",
                "--override", "_test.dblL=[9.1, 9.87]",
                "--override", "_test.arr[0].nst1-1=test",
                "--override", "_test.arr[0].nst1-2=test",
                "--override", "_test.arr[1].nst2-1[0].nst2-1-1=test",
                "alchemist-benchmark/src/jmh/resources/simulation.yml",
            ),
        )
    }
}
