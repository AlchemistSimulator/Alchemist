/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.reactions

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReactionConfigurationTest {
    private val environment = mockk<Environment<Any, *>>(relaxed = true) {
        every { simulationOrNull } returns null
    }
    private val reaction = GenericReaction(mockk<Node<Any>>(relaxed = true), DiracComb(1.0))

    private fun conditionOf(owner: Reaction<Any>): Condition<Any> = mockk(relaxed = true) {
        every { reaction } returns owner
        every { isValid } returns observe(true)
    }

    private fun actionOf(owner: Reaction<Any>): Action<Any> = mockk(relaxed = true) {
        every { reaction } returns owner
    }

    private fun assertConfigurationRejected() {
        assertFailsWith<IllegalStateException> { reaction.conditions = listOf(conditionOf(reaction)) }
        assertFailsWith<IllegalStateException> { reaction.actions = listOf(actionOf(reaction)) }
    }

    @Test
    fun `conditions and actions can be set and replaced before initialization`() {
        reaction.conditions = listOf(conditionOf(reaction))
        reaction.actions = listOf(actionOf(reaction))
        val conditions = listOf(conditionOf(reaction), conditionOf(reaction))
        val actions = listOf(actionOf(reaction))
        reaction.conditions = conditions
        reaction.actions = actions
        reaction.initializationComplete(Time.ZERO, environment)
        assertEquals(conditions, reaction.conditions)
        assertEquals(actions, reaction.actions)
    }

    @Test
    fun `conditions and actions cannot change once the reaction is initialized`() {
        reaction.initializationComplete(Time.ZERO, environment)
        assertConfigurationRejected()
    }

    @Test
    fun `conditions and actions cannot change once the reaction is disposed`() {
        reaction.dispose()
        assertConfigurationRejected()
    }
}
