/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state

import it.unibo.alchemist.boundary.webui.client.state.actions.SetBitmap
import it.unibo.alchemist.boundary.webui.client.state.actions.SetPlayButton
import it.unibo.alchemist.boundary.webui.client.state.actions.SetRenderMode
import it.unibo.alchemist.boundary.webui.client.state.actions.SetStatusSurrogate
import it.unibo.alchemist.boundary.webui.common.model.RenderMode
import it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate
import it.unibo.alchemist.boundary.webui.common.utility.Action
import korlibs.image.bitmap.Bitmap32
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.reduxkotlin.Store
import org.reduxkotlin.createStore

class ClientStoreTest {

    private lateinit var clientStore: Store<ClientState>

    @BeforeTest
    fun setUp() {
        clientStore = createStore(::rootReducer, ClientState())
    }

    @Test
    fun `default configuration at the beginning`() {
        assertEquals(Action.PAUSE, clientStore.state.playButton)
        assertEquals(RenderMode.AUTO, clientStore.state.renderMode)
        assertNull(clientStore.state.bitmap)
        assertEquals(StatusSurrogate.INIT, clientStore.state.statusSurrogate)
    }

    @Test
    fun `can be updated with SetPlayButton action`() {
        listOf(Action.PLAY, Action.PAUSE).forEach { expected ->
            clientStore.dispatch(SetPlayButton(expected))
            assertEquals(expected, clientStore.state.playButton)
        }
    }

    @Test
    fun `can be updated with SetRenderMode action`() {
        listOf(RenderMode.CLIENT, RenderMode.SERVER, RenderMode.AUTO).forEach { expected ->
            clientStore.dispatch(SetRenderMode(expected))
            assertEquals(expected, clientStore.state.renderMode)
        }
    }

    @Test
    fun `can be updated with SetBitmap action`() {
        listOf(
            Bitmap32(1, 1, premultiplied = false),
            Bitmap32(2, 2, premultiplied = false),
        ).forEach { bitmap ->
            clientStore.dispatch(SetBitmap(bitmap))
            assertEquals(bitmap, clientStore.state.bitmap)
        }
    }

    @Test
    fun `can be updated with SetStatusSurrogate action`() {
        listOf(
            StatusSurrogate.READY,
            StatusSurrogate.PAUSED,
            StatusSurrogate.RUNNING,
            StatusSurrogate.TERMINATED,
            StatusSurrogate.INIT,
        ).forEach { expected ->
            clientStore.dispatch(SetStatusSurrogate(expected))
            assertEquals(expected, clientStore.state.statusSurrogate)
        }
    }
}
