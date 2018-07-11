/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.boundary.util

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import java.io.StringWriter
import java.io.PrintWriter
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.GridPane
import javafx.application.Platform
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle
import org.danilopianini.util.stream.Streams
import java.util.stream.Stream
import java.util.stream.Collectors
import javafx.scene.layout.Region

final class FXUtil {
	
    private static final val RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

	private new(){ }

	def static void errorAlert(Thread thread, Throwable ex) {
		val messages = Streams::flatten(ex, [if (it.cause === null) Stream.empty() else Stream.of(it.cause)])
			.map([it.message])
			.filter([it !== null])
			.collect(Collectors::joining('\n'))
		val sw = new StringWriter()
		ex.printStackTrace(new PrintWriter(sw))
		val exceptionText = sw.toString()
		val label = new Label(RESOURCES.getString("debug_information_follows"))
		val textArea = new TextArea(exceptionText)
		val expContent = new GridPane()
		Platform::runLater[
			val alert = new Alert(AlertType.ERROR)
			alert.setTitle(RESOURCES.getString("error_occurred"))
			alert.setHeaderText(RESOURCES.getString("error_follows"))
			alert.setContentText(messages)
			textArea.setEditable(false)
			textArea.setWrapText(true)
			textArea.setMaxWidth(Double.MAX_VALUE)
			textArea.setMaxHeight(Double.MAX_VALUE)
			GridPane.setVgrow(textArea, Priority.ALWAYS)
			GridPane.setHgrow(textArea, Priority.ALWAYS)
			expContent.setMaxWidth(Double.MAX_VALUE)
			expContent.add(label, 0, 0)
			expContent.add(textArea, 0, 1)
			alert.getDialogPane().setExpandableContent(expContent)
			alert.getDialogPane().getChildren()
				.filter[it instanceof Label]
				.forEach[it.minHeight = Region.USE_PREF_SIZE]
				alert.showAndWait()
		]
	}

}
