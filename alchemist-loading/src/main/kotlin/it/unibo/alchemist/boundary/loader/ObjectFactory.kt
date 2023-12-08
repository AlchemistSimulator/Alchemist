/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader

import it.unibo.alchemist.boundary.ExportFilter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.FactoryBuilder
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.Double
import kotlin.Long
import kotlin.Triple
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
                error(
                    "No incarnations have been included, " +
                        "please make sure that your classpath includes at least one module named like " +
                        "it.unibo.alchemist:alchemist-*",
                )
            }
            SupportedIncarnations.get<Nothing, Nothing>(it.toString()).orElseThrow {
                IllegalArgumentException("Unknown incarnation \"$it\". Possible values are $availableIncarnations")
            }
        }
        factory.registerImplicit(CharSequence::class.java, ExportFilter::class.java) {
            CommonFilters.fromString(it.toString())
        }
        /*
         * Numbers, times, and collections
         */
        factory.registerImplicit(Number::class.java, CharSequence::class.java) { it.toString() }
        factory.registerImplicit(Double::class.javaPrimitiveType, Time::class.java) {
            DoubleTime(
                it,
            )
        }
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
