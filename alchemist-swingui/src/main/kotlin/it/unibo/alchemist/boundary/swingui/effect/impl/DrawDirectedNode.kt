/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.swingui.effect.api.Effect
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import org.danilopianini.lang.RangedInteger
import org.danilopianini.view.ExportForGUI
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Polygon
import java.awt.Shape
import java.awt.geom.AffineTransform
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

@Suppress("DEPRECATION")
class DrawDirectedNode : Effect {
    private var positionsMemory: Map<Int, List<Pair<Position2D<*>, Double>>> = emptyMap()
    private var lastDrawMemory: Map<Int, Int> = emptyMap()

    @ExportForGUI(nameToExport = "Track")
    private val trackEnabled: Boolean = true

    @ExportForGUI(nameToExport = "SnapshotSize")
    private val snapshotSize: RangedInteger = RangedInteger(10, MAX_LENGTH, LENGTH)

    @ExportForGUI(nameToExport = "SnapshotFrequency")
    private val timespan: RangedInteger = RangedInteger(1, 100, CLOCK)

    @ExportForGUI(nameToExport = "NodeSize")
    private val nodeSize: RangedInteger = RangedInteger(1, 20, DRONE_SIZE.toInt())

    @ExportForGUI(nameToExport = "Hue Molecule Property")
    private var colorMolecule: String = "hue"

    @ExportForGUI(nameToExport = "Velocity Molecule Property")
    private var velocityMolecule: String = "velocity"

    @ExportForGUI(nameToExport = "Max Value")
    private var maxValue: String = ""

    override fun getColorSummary(): Color = Color.BLACK

    override fun <T : Any, P : Position2D<P>> apply(
        g: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        val nodePosition: P = environment.getPosition(node)
        val viewPoint: Point = wormhole.getViewPoint(nodePosition)
        val (x, y) = Pair(viewPoint.x, viewPoint.y)
        drawDirectedNode(g, node, x, y, environment, wormhole)
    }

    private fun <T : Any, P : Position2D<P>> drawDirectedNode(
        graphics2D: Graphics2D,
        node: Node<T>,
        x: Int,
        y: Int,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        val currentRotation = rotation(node)
        val transform = computeTransform(x, y, nodeSize.`val`.toDouble(), currentRotation)
        val color = computeColorOrBlack(node, environment)
        val transformedShape = transform.createTransformedShape(DRONE_SHAPE)
        if (trackEnabled) drawTrajectory(graphics2D, node, color, wormhole, DRONE_SHAPE)
        graphics2D.color = color
        graphics2D.fill(transformedShape)
        updateTrajectory(node, environment)
    }

    private fun <P : Position2D<P>> drawTrajectory(graphics2D: Graphics2D, node: Node<*>, colorBase: Color, wormhole2D: Wormhole2D<P>, shape: Shape) {
        val positions = positionsMemory[node.id] ?: emptyList()
        val alpha = MAX_COLOR / (min(snapshotSize.`val`, positions.size) * ADJUST_ALPHA_FACTOR + 1)
        positions.takeLast(snapshotSize.`val`).withIndex().forEach { (index, pair) ->
            val (position, rotation) = pair
            val colorFaded =
                Color(colorBase.red, colorBase.green, colorBase.blue, max(1, (alpha * (index + 1)).toInt()))

            @Suppress("UNCHECKED_CAST")
            val transform = computeTransform(wormhole2D.getViewPoint(position as P).x, wormhole2D.getViewPoint(position).y, nodeSize.`val`.toDouble(), rotation)
            val transformedShape = transform.createTransformedShape(shape)
            graphics2D.color = colorFaded
            graphics2D.fill(transformedShape)
        }
    }
    private fun computeTransform(x: Int, y: Int, size: Double, rotation: Double): AffineTransform =
        AffineTransform().apply {
            translate(x.toDouble(), y.toDouble())
            scale(size, size)
            rotate(rotation)
        }

    private fun computeColorOrBlack(node: Node<*>, environment: Environment<*, *>): Color = node
        .takeIf { it.contains(SimpleMolecule(colorMolecule)) }
        ?.getConcentration(SimpleMolecule(colorMolecule))
        ?.let { it as? Number }
        ?.let { it.toDouble() }
        ?.let { Color.getHSBColor((it / (maxValue.toDoubleOrNull() ?: environment.nodeCount.toDouble())).toFloat(), 1f, 1f) }
        ?: Color.BLACK

    private fun <P : Position2D<P>, T> updateTrajectory(node: Node<T>, environment: Environment<T, P>) {
        val positions = positionsMemory[node.id] ?: emptyList()
        val lastDraw = lastDrawMemory[node.id] ?: 0
        val roundedTime = environment.simulation.time.toDouble().toInt()
        if (roundedTime >= lastDraw) {
            lastDrawMemory = lastDrawMemory + (node.id to lastDraw + timespan.`val`)
            positionsMemory = positionsMemory +
                (node.id to (positions + (environment.getPosition(node) to rotation(node))).takeLast(MAX_LENGTH))
        }
    }

    private fun <T> rotation(node: Node<T>): Double = node.takeIf { it.contains(SimpleMolecule(velocityMolecule)) }
        ?.getConcentration(SimpleMolecule(velocityMolecule))
        ?.let { it as? DoubleArray }
        ?.let { atan2(it[0], it[1]) }
        ?: 0.0

    companion object {
        private const val ADJUST_ALPHA_FACTOR: Int = 4

        private const val CLOCK: Int = 10

        private const val LENGTH: Int = 140

        private const val MAX_LENGTH: Int = 1000

        private const val MAX_COLOR: Double = 255.0

        private val DRONE_SHAPE: Polygon = Polygon(intArrayOf(-1, 0, 1), intArrayOf(1, -2, 1), 3)

        private const val DRONE_SIZE = 4.0
    }
}
