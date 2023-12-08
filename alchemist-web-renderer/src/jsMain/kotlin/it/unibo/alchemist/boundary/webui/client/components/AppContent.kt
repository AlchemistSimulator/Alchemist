/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.components

import it.unibo.alchemist.boundary.webui.client.logic.HwAutoRenderModeStrategy
import it.unibo.alchemist.boundary.webui.client.logic.RESTUpdateStateStrategy
import it.unibo.alchemist.boundary.webui.client.logic.updateState
import it.unibo.alchemist.boundary.webui.client.state.ClientStore.store
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.toHtmlNative
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.useEffectOnce
import react.useState

private val scope = MainScope()
private const val UPDATE_STATE_DELAY: Long = 100

/**
 * The application main content section.
 */
val AppContent: FC<Props> = FC {

    var bitmap: Bitmap? by useState(null)

    store.subscribe {
        bitmap = store.state.bitmap
    }

    useEffectOnce {
        scope.launch {
            while (true) {
                updateState(store.state.renderMode, RESTUpdateStateStrategy(), HwAutoRenderModeStrategy())
                delay(UPDATE_STATE_DELAY)
            }
        }
    }

    div {
        img {
            src = bitmap?.toHtmlNative()?.lazyCanvasElement?.toDataURL()
        }
    }
}
