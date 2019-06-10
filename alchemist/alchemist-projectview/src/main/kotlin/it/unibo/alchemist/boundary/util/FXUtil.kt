/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.util

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle
import it.unibo.alchemist.kotlin.unfold
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import java.io.PrintWriter
import java.io.StringWriter

class FXUtil {
    companion object {
        @JvmStatic
        private val RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings")

        @JvmStatic fun errorAlert(ex: Throwable) {
            val messages = ex
                .unfold { when (it.cause) {
                    null -> emptySequence()
                    else -> sequenceOf(it)
                } }
                .map { it.message }
                .filter { it != null }
                .joinToString("\n")
            val strWriter = StringWriter()
            ex.printStackTrace(PrintWriter(strWriter))
            val exceptionTest = strWriter.toString()
            val label = Label(RESOURCES.getString("debug_information_follows"))
            val textArea = TextArea(exceptionTest)
            val expContent = GridPane()
            Platform.runLater {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = RESOURCES.getString("error_occurred")
                alert.headerText = RESOURCES.getString("error_follows")
                alert.contentText = messages
                textArea.isEditable = true
                textArea.isWrapText = true
                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.dialogPane.children.filter { it is Label }
                    .forEach { it.minHeight(Region.USE_PREF_SIZE) }
                alert.showAndWait()
            }
        }
    }
}