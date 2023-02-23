/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.components

import react.FC
import it.unibo.alchemist.client.adapters.reactBootstrap.modal.Modal
import it.unibo.alchemist.client.adapters.reactBootstrap.modal.ModalBody
import it.unibo.alchemist.client.adapters.reactBootstrap.modal.ModalHeader
import it.unibo.alchemist.client.adapters.reactBootstrap.modal.ModalProps
import it.unibo.alchemist.client.adapters.reactBootstrap.modal.ModalTitle

/**
 * Modal used to show information about something that didn't work as expected.
 */
val WarningModal: FC<WarningModalProps> = FC { props ->
    Modal {
        show = props.show
        onHide = props.onHide
        ModalHeader {
            closeButton = true
            ModalTitle {
                className = "text-danger"
                +props.title
            }
        }
        ModalBody {
            +props.message
        }
    }
}

/**
 * Props used to customize the WarningModal.
 */
external interface WarningModalProps : ModalProps {
    /**
     * Title of the Modal.
     */
    var title: String

    /**
     * Message of the Modal.
     */
    var message: String
}
