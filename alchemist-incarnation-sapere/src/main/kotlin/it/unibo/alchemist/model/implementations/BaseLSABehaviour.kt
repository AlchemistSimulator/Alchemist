/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations

import it.unibo.alchemist.expressions.implementations.Expression
import it.unibo.alchemist.expressions.implementations.NumTreeNode
import it.unibo.alchemist.expressions.interfaces.IExpression
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule
import it.unibo.alchemist.model.interfaces.ILsaMolecule
import it.unibo.alchemist.model.interfaces.LSABehaviour
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import java.util.Collections

/**
 * Base implementation of [LSABehaviour].
 */
class BaseLSABehaviour(
    override val node: Node<ILsaMolecule>,
) : LSABehaviour {
    private val instances: MutableList<ILsaMolecule> = mutableListOf()

    override fun setConcentration(instance: ILsaMolecule) {
        if (instance.isIstance) {
            instances.add(instance)
        } else {
            throw IllegalStateException("Tried to insert uninstanced $instance into $this")
        }
    }

    override fun contains(molecule: Molecule): Boolean {
        return if (molecule is ILsaMolecule) {
            instances.any { molecule.matches(it) }
        } else false
    }

    override val moleculeCount: Int
        get() = instances.size

    override fun getConcentration(molecule: Molecule): List<ILsaMolecule> {
        return if (molecule is ILsaMolecule) {
            instances.filter { molecule.matches(it) }.toList()
        } else {
            throw IllegalArgumentException("$molecule is not a compatible molecule type")
        }
    }

    override fun removeConcentration(matchedInstance: ILsaMolecule): Boolean =
        instances.remove(instances.first { matchedInstance.matches(it) })

    override fun getLsaSpace(): List<ILsaMolecule> = Collections.unmodifiableList(instances)

    override fun getContents(): Map<Molecule, List<ILsaMolecule>> {
        val result: MutableMap<Molecule, MutableList<ILsaMolecule>> = HashMap(instances.size, 1.0f)
        instances.forEach { molecule ->
            val list: MutableList<ILsaMolecule>
            if (result.containsKey(molecule)) {
                /*
                 * Safe by construction.
                 */
                list = result[molecule]!!
            } else {
                list = mutableListOf()
                list.add(ZEROMOL)
                result[molecule] = list
            }
            val v = list[0].getArg(0).rootNodeData as Double + 1
            val e: IExpression = Expression(NumTreeNode(v))
            list[0] = LsaMolecule(listOf(e))
        }
        return result
    }

    companion object {
        private val ZEROMOL: ILsaMolecule = LsaMolecule("0")
    }
}
