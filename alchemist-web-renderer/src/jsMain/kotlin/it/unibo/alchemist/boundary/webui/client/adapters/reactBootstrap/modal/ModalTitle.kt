/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JsModule("react-bootstrap/ModalTitle")
@file:JsNonModule

package it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.modal

import react.FC
import react.PropsWithChildren

/**
 * React Bootstrap ModalTitle adapter.
 * @see <a href="https://react-bootstrap.github.io/docs/components/modal">
 *     react-bootstrap - modal</a>
 */

@JsName("default")
external val ModalTitle: FC<ModalTitleProps>

/**
 * Props used to customize the ModalHeader.
 */
external interface ModalTitleProps : PropsWithChildren {
    /**
     * className prop. Useful for customizing.
     */
    var className: String
}
