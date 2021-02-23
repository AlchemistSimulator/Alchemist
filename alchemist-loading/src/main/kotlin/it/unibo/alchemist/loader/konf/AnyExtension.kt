/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import it.unibo.alchemist.loader.konf.types.JVMConstructor
import it.unibo.alchemist.loader.konf.types.VariableDescriptor

val Any?.requireNull get() = require(this == null) {
    "Illegal non-null value: $this"
}

fun requireAllNulls(vararg objects: Any?) = objects.also {
    objects.forEach { it.requireNull }
}

//fun Any.resolve(): Any =
//    if (this is Map<*, *>) {
//        val type = this["type"]
//        if (type != null) {
//            val parameters = this["parameters"] as? Iterable<*>
//            JVMConstructor.create(type, parameters)
//        } else  {
//            val formula = this["formula"]
//            if (formula != null) {
//                VariableDescriptor.create(formula = formula, language = this["language"])
//            } else {
//                val min = this["min"]
//                val max = this["max"]
//                val step = this["step"]
//                val default = this["default"]
//                if (min != null && max != null && step != null && default != null) {
//                    VariableDescriptor.create(min, max, step, default)
//                }
//            }
//        }
//    } else {
//        this
//    }
