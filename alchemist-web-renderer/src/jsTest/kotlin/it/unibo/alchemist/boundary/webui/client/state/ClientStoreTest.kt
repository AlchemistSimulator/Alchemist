/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state

import com.soywiz.korim.bitmap.Bitmap32
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.client.state.actions.SetBitmap
import it.unibo.alchemist.boundary.webui.client.state.actions.SetPlayButton
import it.unibo.alchemist.boundary.webui.client.state.actions.SetRenderMode
import it.unibo.alchemist.boundary.webui.client.state.actions.SetStatusSurrogate
import it.unibo.alchemist.boundary.webui.common.model.RenderMode
import it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate
import it.unibo.alchemist.boundary.webui.common.utility.Action
import org.reduxkotlin.Store
import org.reduxkotlin.createStore

class ClientStoreTest : StringSpec({

    val clientStore: Store<ClientState> = createStore(::rootReducer, ClientState())

    "ClientStore should have a default configuration at the beginning" {
        clientStore.state.playButton shouldBe Action.PAUSE
        clientStore.state.renderMode shouldBe RenderMode.AUTO
        clientStore.state.bitmap shouldBe null
        clientStore.state.statusSurrogate shouldBe StatusSurrogate.INIT
    }

    "ClientStore can be updated with a SetPlayButton action" {
        listOf(Action.PLAY, Action.PAUSE).forEach {
            clientStore.dispatch(SetPlayButton(it))
            clientStore.state.playButton shouldBe it
        }
    }

    "ClientStore can be updated with a SetRenderMode action" {
        listOf(RenderMode.CLIENT, RenderMode.SERVER, RenderMode.AUTO).forEach {
            clientStore.dispatch(SetRenderMode(it))
            clientStore.state.renderMode shouldBe it
        }
    }

    "ClientStore can be updated with a SetBitmap action" {
        listOf(Bitmap32(1, 1, premultiplied = false), Bitmap32(2, 2, premultiplied = false)).forEach {
            clientStore.dispatch(SetBitmap(it))
            clientStore.state.bitmap shouldBe it
        }
    }

    "ClientStore can be updated with a SetStatusSurrogate action" {
        listOf(
            StatusSurrogate.READY,
            StatusSurrogate.PAUSED,
            StatusSurrogate.RUNNING,
            StatusSurrogate.TERMINATED,
            StatusSurrogate.INIT,
        ).forEach {
            clientStore.dispatch(SetStatusSurrogate(it))
            clientStore.state.statusSurrogate shouldBe it
        }
    }
})
