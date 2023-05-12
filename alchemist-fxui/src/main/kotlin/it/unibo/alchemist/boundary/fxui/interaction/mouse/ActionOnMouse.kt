/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.mouse

/**
 * Actions that can happen on a mouse and a certain mouse button.
 * The enum's values are based on JavaFX's mouse events, such as onMouseClicked
 */
enum class ActionOnMouse {
    CLICKED,
    DRAGGED,
    ENTERED,
    EXITED,
    MOVED,
    PRESSED,
    RELEASED,
}
