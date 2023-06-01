/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JsModule("react-bootstrap/Navbar")
@file:JsNonModule

package it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.navbar

import react.ComponentClass
import react.Props

/**
 * React Bootstrap Navbar adapter.
 * @see <a href="https://react-bootstrap.github.io/docs/components/navbar/">react-bootstrap - navbar</a>
 */
@JsName("default")
external val Navbar: ComponentClass<NavbarProps>

/**
 * Props used to customize the Navbar.
 */
external interface NavbarProps : Props {
    /**
     * bg props.
     */
    var bg: String

    /**
     * variant props.
     */
    var variant: String
}
