/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JsModule("react-bootstrap/ModalHeader")
@file:JsNonModule

package it.unibo.alchemist.client.adapters.reactBootstrap.modal

import react.ComponentClass
import react.Props

/**
 * React Bootstrap ModalHeader adapter.
 * @see <a href="https://react-bootstrap.github.io/components/modal">
 *     react-bootstrap - modal</a>
 */

@JsName("default")
external val ModalHeader: ComponentClass<ModalHeaderProps>

/**
 * Props used to customize the ModalHeader.
 */
external interface ModalHeaderProps : Props {
    /**
     * closeButton prop.
     */
    var closeButton: Boolean?
}
