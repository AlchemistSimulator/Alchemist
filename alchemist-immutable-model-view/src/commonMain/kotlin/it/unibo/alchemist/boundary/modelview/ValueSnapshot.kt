/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A value of the simulation: its text, plus its structure for numbers, booleans, collections and maps,
 * so that they can be used without parsing.
 * Edits go back as text, parsed by the simulation.
 */
@Serializable
sealed interface ValueSnapshot {
    /** The value as text. */
    val text: String

    /**
     * A number.
     *
     * @property value the number.
     * @property text the number as the simulation writes it, such as `3` for an integer.
     */
    @Serializable
    @SerialName("number")
    data class Numeric(
        val value: Double,
        override val text: String,
    ) : ValueSnapshot

    /**
     * A boolean.
     *
     * @property value the boolean.
     */
    @Serializable
    @SerialName("boolean")
    data class Flag(
        val value: Boolean,
    ) : ValueSnapshot {
        override val text: String get() = value.toString()
    }

    /**
     * Any other value, as text, possibly cut.
     *
     * @property text the value as text.
     */
    @Serializable
    @SerialName("text")
    data class Text(
        override val text: String,
    ) : ValueSnapshot

    /**
     * A collection, such as a list, a set, a tuple, or an array.
     *
     * @property items the first items, possibly not all of them.
     * @property size how many items there are: more than [items] if cut.
     * @property unique whether it is a set.
     */
    @Serializable
    @SerialName("items")
    data class Items(
        val items: List<ValueSnapshot>,
        val size: Int = items.size,
        val unique: Boolean = false,
    ) : ValueSnapshot {
        override val text: String
            get() = items.map(ValueSnapshot::text).listed(cut = size > items.size, prefix = "[", postfix = "]")
    }

    /**
     * A map.
     *
     * @property entries the first entries, possibly not all of them.
     * @property size how many entries there are: more than [entries] if cut.
     */
    @Serializable
    @SerialName("entries")
    data class Entries(
        val entries: List<EntrySnapshot>,
        val size: Int = entries.size,
    ) : ValueSnapshot {
        override val text: String
            get() = entries
                .map { "${it.key.text}=${it.value.text}" }
                .listed(cut = size > entries.size, prefix = "{", postfix = "}")
    }

    /** Builds values. */
    companion object {
        /**
         * [value] as a [ValueSnapshot], within [limits].
         * Maps, iterables and arrays become [Entries] and [Items], with only their size beyond the depth limit;
         * other values that are not numbers or booleans, such as Scala collections, become [Text].
         */
        fun of(value: Any?, limits: ValueLimits = ValueLimits()): ValueSnapshot = of(value, limits, limits.depth)

        private fun of(value: Any?, limits: ValueLimits, depth: Int): ValueSnapshot {
            val kept = if (depth > 0) limits.items else 0
            val items = value.itemsOrNull()
            return when {
                value is Number -> Numeric(value.toDouble(), value.toString())
                value is Boolean -> Flag(value)
                value is Map<*, *> -> Entries(
                    entries = value.entries.take(kept).map { (key, entry) ->
                        EntrySnapshot(of(key, limits, depth - 1), of(entry, limits, depth - 1))
                    },
                    size = value.size,
                )
                items != null -> Items(
                    items = items.take(kept).map { of(it, limits, depth - 1) },
                    size = if (items is Collection<*>) items.size else items.count(),
                    unique = value is Set<*>,
                )
                else -> Text(value.toString().cutAt(limits.textLength))
            }
        }
    }
}

private const val CUT = "…"

private fun Any?.itemsOrNull(): Iterable<*>? = when (this) {
    is Iterable<*> -> this
    is Array<*> -> asList()
    is IntArray -> asList()
    is LongArray -> asList()
    is DoubleArray -> asList()
    is FloatArray -> asList()
    is ShortArray -> asList()
    is ByteArray -> asList()
    is CharArray -> asList()
    is BooleanArray -> asList()
    else -> null
}

private fun String.cutAt(length: Int): String = if (this.length > length) take(length) + CUT else this

private fun List<String>.listed(cut: Boolean, prefix: String, postfix: String): String =
    (this + listOfNotNull(CUT.takeIf { cut })).joinToString(prefix = prefix, postfix = postfix)
