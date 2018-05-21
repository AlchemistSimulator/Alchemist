package it.unibo.alchemist.kotlin

fun <E> E.unfold(extractor: (E) -> Sequence<E>): Sequence<E> =
    sequenceOf(this) + extractor(this).flatMap { it.unfold(extractor) }