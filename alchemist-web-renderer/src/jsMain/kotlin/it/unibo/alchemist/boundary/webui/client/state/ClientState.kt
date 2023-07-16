/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state

import com.soywiz.korim.bitmap.Bitmap
import it.unibo.alchemist.boundary.webui.client.state.reducers.bitmapReducer
import it.unibo.alchemist.boundary.webui.client.state.reducers.playButtonReducer
import it.unibo.alchemist.boundary.webui.client.state.reducers.renderModeReducer
import it.unibo.alchemist.boundary.webui.client.state.reducers.statusSurrogateReducer
import it.unibo.alchemist.boundary.webui.common.model.RenderMode
import it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate
import it.unibo.alchemist.boundary.webui.common.state.CommonState
import it.unibo.alchemist.boundary.webui.common.utility.Action

/**
 * The state of the client.
 * The [ClientState] is managed using the
 * [Core concepts of the ReduxKotlin library](https://reduxkotlin.org/introduction/core-concepts).
 * Like in the original Redux library,
 * the state is stored in a single class that contains other objects via composition.
 * The state can be changed using actions that must be defined in advance.
 * The unique store that encapsulate the [ClientState] is
 * [it.unibo.alchemist.boundary.webui.client.state.ClientStore].
 *
 * @param renderMode the render mode of the client.
 * It can be either client, server or auto. It is set to auto by default.
 * @param playButton the state of the play button.
 * @param bitmap the bitmap to display.
 * @param statusSurrogate the [StatusSurrogate] of the simulation.
 * @see <a href="https://reduxkotlin.org/">ReduxKotlin Documentation</a>
 */
data class ClientState(
    val renderMode: RenderMode = RenderMode.AUTO,
    val playButton: Action = Action.PAUSE,
    val bitmap: Bitmap? = null,
    val statusSurrogate: StatusSurrogate = StatusSurrogate.INIT,
) : CommonState()

/**
 * Root reducer of the client. Uses all the other reducers.
 * @param state the current state of the application.
 * @param action the action to perform.
 * @return the new state of the application.
 */
fun rootReducer(state: ClientState, action: Any): ClientState = ClientState(
    renderMode = renderModeReducer(state.renderMode, action),
    playButton = playButtonReducer(state.playButton, action),
    bitmap = bitmapReducer(state.bitmap, action),
    statusSurrogate = statusSurrogateReducer(state.statusSurrogate, action),
)
