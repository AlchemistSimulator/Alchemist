/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard

/**
 * Actions which can be bound to a key on the keyboard.
 */
enum class ActionFromKey(private val description: String) {
    MODIFIER_CONTROL("Control modifier"),
    MODIFIER_SHIFT("Shift modifier"),
    PAN_NORTH("Pan north"),
    PAN_SOUTH("Pan south"),
    PAN_EAST("Pan east"),
    PAN_WEST("Pan west"),
    DELETE("Delete"),
    MOVE("Move"),
    EDIT("Edit"),
    PLAY_AND_PAUSE("Play and Pause"),
    ONE_STEP("Forward one step"),
    ;

    override fun toString() = description
}
