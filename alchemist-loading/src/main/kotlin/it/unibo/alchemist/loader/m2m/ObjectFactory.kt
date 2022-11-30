/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.m2m

import it.unibo.alchemist.loader.export.FilteringPolicy
import it.unibo.alchemist.loader.export.filters.CommonFilters
import it.unibo.alchemist.model.api.SupportedIncarnations
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.FactoryBuilder
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.Array
import kotlin.CharSequence
import kotlin.Double
import kotlin.Long
import kotlin.Number
import kotlin.Pair
import kotlin.String
import kotlin.Triple
import kotlin.toBigInteger
import org.apache.commons.lang3.tuple.Pair as CommonsLangPair
import org.apache.commons.lang3.tuple.Triple as CommonsLangTriple
import org.apache.commons.math3.util.Pair as CommonsMathPair

/**
 *
 */
internal object ObjectFactory {

    fun makeBaseFactory(): Factory {
        val factory = FactoryBuilder()
            .withNarrowingConversions()
            .withArrayBooleanIntConversions()
            .withArrayListConversions(Array<String>::class.java, Array<Number>::class.java)
            .withArrayNarrowingConversions()
            .withAutomaticToString()
            .build()
        /*
         * Alchemist entities
         */
        factory.registerImplicit(CharSequence::class.java, Incarnation::class.java) {
            val availableIncarnations = SupportedIncarnations.getAvailableIncarnations()
            if (availableIncarnations.isEmpty()) {
                throw IllegalStateException(
                    "No incarnations have been included, " +
                        "please make sure that your class path includes at least one module named like " +
                        "it.unibo.alchemist:alchemist-*"
                )
            }
            SupportedIncarnations.get<Nothing, Nothing>(it.toString()).orElseThrow {
                IllegalArgumentException("Unknown incarnation \"$it\". Possible values are $availableIncarnations")
            }
        }
        factory.registerImplicit(CharSequence::class.java, FilteringPolicy::class.java) {
            CommonFilters.fromString(it.toString())
        }
        /*
         * Numbers, times, and collections
         */
        factory.registerImplicit(Number::class.java, CharSequence::class.java) { it.toString() }
        factory.registerImplicit(Double::class.javaPrimitiveType, Time::class.java) { DoubleTime(it) }
        factory.registerImplicit(List::class.java, Array<Number>::class.java) { list ->
            list.map { factory.convertOrFail(Number::class.java, it) }.toTypedArray()
        }
        factory.registerImplicit(Number::class.java, Double::class.javaPrimitiveType) { it.toDouble() }
        factory.registerImplicit(Double::class.javaPrimitiveType, BigDecimal::class.java) { BigDecimal(it) }
        factory.registerImplicit(Long::class.javaPrimitiveType, BigInteger::class.java) { it.toBigInteger() }
        /*
         * Pairs
         */
        factory.registerImplicit(List::class.java, Pair::class.java) {
            if (it.size == 2) {
                Pair(it[0], it[1])
            } else {
                error("Only a two argument list can be converted to a Pair. Provided: $it")
            }
        }
        factory.registerImplicit(Pair::class.java, CommonsMathPair::class.java) { CommonsMathPair(it.first, it.second) }
        factory.registerImplicit(CommonsMathPair::class.java, Pair::class.java) { Pair(it.first, it.second) }
        factory.registerImplicit(Pair::class.java, CommonsLangPair::class.java) { ImmutablePair(it.first, it.second) }
        factory.registerImplicit(CommonsLangPair::class.java, Pair::class.java) { Pair(it.left, it.right) }
        /*
         * Triples
         */
        factory.registerImplicit(List::class.java, Triple::class.java) {
            if (it.size == 3) {
                val (a, b, c) = it
                Triple(a, b, c)
            } else {
                error("Only a two argument list can be converted to a Pair. Provided: $it")
            }
        }
        factory.registerImplicit(Triple::class.java, CommonsLangTriple::class.java) { (first, second, third) ->
            ImmutableTriple(first, second, third)
        }
        factory.registerImplicit(CommonsLangTriple::class.java, Triple::class.java) {
            Triple(it.left, it.middle, it.right)
        }
        return factory
    }
}
