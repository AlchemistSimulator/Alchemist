package it.unibo.alchemist.boundary.dsl.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeParameterHandlerTest {
    @Test
    fun `test buildTypeParamString with single parameter`() {
        val typeParamBounds = listOf("T")
        val result = TypeParameterHandler.buildTypeParamString(typeParamBounds)

        assertEquals("<T>", result)
    }

    @Test
    fun `test buildTypeParamString with multiple parameters`() {
        val typeParamBounds = listOf("T", "P: it.unibo.alchemist.model.Position<P>")
        val result = TypeParameterHandler.buildTypeParamString(typeParamBounds)

        assertEquals("<T, P: it.unibo.alchemist.model.Position<P>>", result)
    }

    @Test
    fun `test buildTypeParamString with empty list`() {
        val typeParamBounds = emptyList<String>()
        val result = TypeParameterHandler.buildTypeParamString(typeParamBounds)

        assertEquals("", result)
    }

    @Test
    fun `test buildReturnType with type parameters`() {
        val className = "TestClass"
        val typeParamNames = listOf("T", "P")
        val result = TypeParameterHandler.buildReturnType(className, typeParamNames)

        assertEquals("TestClass<T, P>", result)
    }

    @Test
    fun `test buildReturnType without type parameters`() {
        val className = "TestClass"
        val typeParamNames = emptyList<String>()
        val result = TypeParameterHandler.buildReturnType(className, typeParamNames)

        assertEquals("TestClass", result)
    }

    @Test
    fun `test findTAndPParams`() {
        val typeParamNames = listOf("T", "P")
        val typeParamBounds = listOf("T", "P: it.unibo.alchemist.model.Position<P>")

        val (tParam, pParam) = TypeParameterHandler.findTAndPParams(typeParamNames, typeParamBounds)

        assertEquals("T", tParam)
        assertEquals("P", pParam)
    }

    @Test
    fun `test findTAndPParams with only T`() {
        val typeParamNames = listOf("T")
        val typeParamBounds = listOf("T")

        val (tParam, pParam) = TypeParameterHandler.findTAndPParams(typeParamNames, typeParamBounds)

        assertEquals("T", tParam)
        assertEquals("P", pParam)
    }
}
