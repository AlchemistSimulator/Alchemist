/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.monitor.impl

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities

/**
 * @param node the node to track.
 * @param P position type.
 * @param T concentration type.
 */
@Deprecated("This class is deprecated anyway")
class NodeTracker<T, P : Position<out P>>(private val node: Node<T>) : JPanel(), OutputMonitor<T, P>, ActionListener {
    private val jTextArea = JTextArea(AREA_SIZE / 2, AREA_SIZE)
    private var stringLength = Byte.MAX_VALUE.toInt()
    private val updateIsScheduled = AtomicBoolean(false)

    @Volatile
    private var currentText: String = ""

    init {
        val areaScrollPane = JScrollPane(jTextArea)
        layout = BorderLayout(0, 0)
        jTextArea.isEditable = false
        add(areaScrollPane, BorderLayout.CENTER)
        areaScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        areaScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
    }

    override fun actionPerformed(e: ActionEvent) = Unit

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        stepDone(environment, null, time, step)
    }

    override fun initialized(environment: Environment<T, P>) {
        stepDone(environment, null, Time.ZERO, 0L)
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        if (reaction == null || reaction is Reaction<*> && reaction.node == node) {
            val content = """
                |$POSITION
                |${environment.getPosition(node)}
                |
                |$CONTENT
                |${node.contents.map { (k, v) -> "${k.name} -> $v" }.sorted().joinToString(System.lineSeparator())}
                |
                |$PROGRAM
                |${node.reactions.joinToString(System.lineSeparator()) { it.toString() }}
            """.trimMargin()
            stringLength = content.length + MARGIN
            currentText = content
            if (!updateIsScheduled.get()) {
                updateIsScheduled.set(true)
                scheduleUpdate()
            }
        }
    }

    private fun scheduleUpdate() {
        SwingUtilities.invokeLater {
            if (updateIsScheduled.getAndSet(false)) {
                jTextArea.text = currentText
            }
        }
    }

    companion object {
        private const val MARGIN: Byte = 100
        private const val PROGRAM = " = Program ="
        private const val CONTENT = " = Content ="
        private const val POSITION = " = POSITION = "
        private const val serialVersionUID = -676002989218532788L
        private const val AREA_SIZE = 80
    }
}
