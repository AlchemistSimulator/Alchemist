/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.logic

import it.unibo.alchemist.common.model.RenderMode
import kotlinx.browser.window

/**
 *  A strategy to assign what [RenderMode] is selected if [RenderMode.AUTO] is selected.
 *  This interface could implement () -> RenderMode but this is prohibited in Kotlin/JS.
 */
interface AutoRenderModeStrategy {
    /**
     * @return the correct [RenderMode].
     */
    operator fun invoke(): RenderMode
}

/**
 * The render mode is selected based on the hardware capacity of the client.
 * @param minHwConcurrency the recommended number of core to use for the Auto mode.
 * @return [RenderMode.CLIENT] if the hardware capacity is above the parameters, [RenderMode.SERVER] otherwise.
 */
data class HwAutoRenderModeStrategy(val minHwConcurrency: Int = 4) : AutoRenderModeStrategy {
    override fun invoke(): RenderMode = if (window.navigator.hardwareConcurrency.toInt() > minHwConcurrency) {
        RenderMode.CLIENT
    } else {
        RenderMode.SERVER
    }
}
