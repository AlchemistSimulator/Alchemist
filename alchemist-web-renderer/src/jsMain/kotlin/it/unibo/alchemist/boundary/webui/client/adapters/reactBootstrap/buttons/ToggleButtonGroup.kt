/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JsModule("react-bootstrap/ToggleButtonGroup")
@file:JsNonModule

package it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.buttons

import react.ComponentClass
import react.PropsWithChildren

/**
 * React Bootstrap ToggleButtonGroup adapter.
 * @see <a href="https://react-bootstrap.github.io/docs/components/buttons/">
 *     react-bootstrap - buttons</a>
 */

@JsName("default")
external val ToggleButtonGroup: ComponentClass<ToggleButtonGroupProps>

/**
 * Props used to customize the ToggleButtonGroup.
 */
external interface ToggleButtonGroupProps : PropsWithChildren {
    /**
     * type prop, 'checkbox' | 'radio'.
     */
    var type: String

    /**
     * size prop to choose buttons size, 'sm' | 'lg'.
     */
    var size: String

    /**
     * name prop.
     */
    var name: String

    /**
     * onChange prop, callback function.
     */
    var onChange: (Any) -> Unit

    /**
     * defaultValue prop.
     */
    var defaultValue: Any
}
