/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JsModule("react-bootstrap/Button")
@file:JsNonModule

package it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.buttons

import react.ComponentClass
import react.Props

/**
 * React Bootstrap Button adapter.
 * @see <a href="https://react-bootstrap.github.io/components/buttons/">
 *     react-bootstrap - buttons</a>
 */

@JsName("default")
external val Button: ComponentClass<ButtonProps>

/**
 * Props used to customize the Button.
 */
external interface ButtonProps : Props {

    /**
     * active prop, false by default.
     */
    var active: Boolean?

    /**
     * disable prop, false by default.
     */
    var disabled: Boolean?

    /**
     * variant prop, 'primary' by default.
     */
    var variant: String

    /**
     * onClick prop. Execute the specified function when clicked.
     */
    var onClick: () -> Unit
}
