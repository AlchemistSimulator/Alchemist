package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.test.AlchemistTesting.loadAlchemistFromResource

class TestConstructorOverloading : StringSpec({
    "constructor overloading should not be ambiguous during loading" {
        val loader = loadAlchemistFromResource("regression/2024-depots-ambiguous-constructors.yml")
        loader shouldNot beNull()
        loader.getDefault<Nothing, Nothing>().environment.shouldNotBeNull()
    }
})
