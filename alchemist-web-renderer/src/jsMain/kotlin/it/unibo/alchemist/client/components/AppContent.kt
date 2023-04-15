/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.components

import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.format.toHtmlNative
import it.unibo.alchemist.client.logic.HwAutoRenderModeStrategy
import it.unibo.alchemist.client.logic.RESTUpdateStateStrategy
import it.unibo.alchemist.client.logic.updateState
import it.unibo.alchemist.client.state.ClientStore.store
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
