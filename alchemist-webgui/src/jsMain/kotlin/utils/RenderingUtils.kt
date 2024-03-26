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

/**
 * Length of the generated color string.
 */
const val COLOR_LENGTH = 6

/**
 * Maximum index for selecting characters from the letters string.
 */
const val MAX_INDEX = 15

/**
 * Checks if the mouse pointer is over a node position within the given radius.
 *
 * @param mouseX The x-coordinate of the mouse pointer.
 * @param mouseY The y-coordinate of the mouse pointer.
 * @param nodeX The x-coordinate of the node position.
 * @param nodeY The y-coordinate of the node position.
 * @param radius The radius of the node.
 * @return True if the mouse pointer is over the node position, false otherwise.
 */

fun isMouseOverNodePosition(mouseX: Double, mouseY: Double, nodeX: Double, nodeY: Double, radius: Double): Boolean {
    val dx = mouseX - nodeX
    val dy = mouseY - nodeY

    return dx * dx + dy * dy <= radius * radius
}

/**
 * Generates a random hexadecimal color string.
 *
 * @return A random hexadecimal color string.
 */
fun randomColor(): String {
    val letters = "0123456789ABCDEF"
    var color = "#"
    repeat(COLOR_LENGTH) { color += letters[Random.nextInt(MAX_INDEX)] }
    return color
}
