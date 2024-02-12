/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package utils

import kotlin.random.Random

fun isMouseOverNodePosition(mouseX: Double, mouseY: Double, nodeX: Double, nodeY: Double, radius: Double): Boolean {
    val dx = mouseX - nodeX
    val dy = mouseY - nodeY

    return dx * dx + dy * dy <= radius * radius
}

fun randomColor(): String {
    val letters = "0123456789ABCDEF"
    var color = "#"
    repeat(6) { color += letters[Random.nextInt(15)] }
    return color
}
