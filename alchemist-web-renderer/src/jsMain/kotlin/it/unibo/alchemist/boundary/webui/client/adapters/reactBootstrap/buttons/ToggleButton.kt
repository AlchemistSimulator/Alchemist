/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JsModule("react-bootstrap/ToggleButton")
@file:JsNonModule

package it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.buttons

import react.ComponentClass
import react.Props

/**
 * React Bootstrap ToggleButton adapter.
 * @see <a href="https://react-bootstrap.github.io/docs/components/buttons/">
 *     react-bootstrap - buttons</a>
 */

@JsName("default")
external val ToggleButton: ComponentClass<ToggleButtonProps>

/**
 * Props used to customize the ToggleButton.
 */
external interface ToggleButtonProps : Props {

    /**
     * id props, required.
     */
    var id: String

    /**
     * value props.
     */
    var value: Any
}
