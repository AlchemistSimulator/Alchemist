/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter.multivesta

object MultiVestaEntryPoint {
    fun launch(args: Array<String>) {
        // entrypointmultivesta.UniqueEntryPoint.main(args)
        val clazz = Class.forName("entrypointmultivesta.UniqueEntryPoint")
        val main = clazz.getDeclaredMethod("main", Array<String>::class.java)
        main.invoke(null, args)
    }
}
