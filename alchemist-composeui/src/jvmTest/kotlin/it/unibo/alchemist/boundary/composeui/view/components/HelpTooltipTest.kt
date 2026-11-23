/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HelpTooltipTest {
    @Test
    fun `a click shows the help, and another hides it`() = runComposeUiTest {
        setContent { HelpTooltip("What this section is for") }
        onNodeWithText("What this section is for").assertDoesNotExist()
        onNodeWithContentDescription("Help").performClick()
        onNodeWithText("What this section is for").assertExists()
        onNodeWithContentDescription("Help").performClick()
        onNodeWithText("What this section is for").assertDoesNotExist()
    }
}
