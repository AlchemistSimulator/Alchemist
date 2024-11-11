package it.unibo.alchemist.test

import another.location.MyTestEnv
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.types.instanceOf
import it.unibo.alchemist.test.AlchemistTesting.loadAlchemistFromResource

class TestConstructorOverloading : StringSpec({
    "constructor overloading should not be ambiguous during loading" {
        val loader = loadAlchemistFromResource("regression/2024-depots-ambiguous-constructors.yml")
        loader shouldNot beNull()
        loader.getDefault<Nothing, Nothing>().environment shouldBe instanceOf(MyTestEnv::class)
    }
})
